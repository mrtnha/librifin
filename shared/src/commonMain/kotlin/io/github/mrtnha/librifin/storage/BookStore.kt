package io.github.mrtnha.librifin.storage

import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * What Librifin keeps on the device: the downloaded books, where each book was left off, and the
 * last loaded book list so the library shows up instantly and while the server can't be reached.
 * Positions and the book list belong to one user on one server, so another login never sees them.
 * Blocking file I/O (except the suspend functions): call it off the main thread.
 */
class BookStore(filesDir: String) {
    private val booksDir = Path(filesDir, "books")
    private val libraryFile = Path(filesDir, "library.json")
    private val positionsRoot = Path(filesDir, "positions")

    /** Saves run one at a time, in the order they were requested, so an older position never wins. */
    private val positionLock = Mutex()

    fun bookFile(bookId: String) = Path(booksDir, "$bookId.epub")

    /** Where a download is written until it's complete, so a broken one is never mistaken for the book. */
    fun partialBookFile(bookId: String): Path {
        SystemFileSystem.createDirectories(booksDir)
        return Path(booksDir, "$bookId.epub.part")
    }

    fun isDownloaded(bookId: String) = SystemFileSystem.exists(bookFile(bookId))

    fun deleteBook(bookId: String) = SystemFileSystem.delete(bookFile(bookId), mustExist = false)

    /** The book list saved for this user on this server, or null if there is none (or it's unreadable). */
    fun readLibrary(session: Session): List<Book>? {
        if (!SystemFileSystem.exists(libraryFile)) return null
        val saved = try {
            JellyfinClient.json.decodeFromString<SavedLibrary>(readText(libraryFile))
        } catch (_: SerializationException) {
            return null
        } catch (_: IllegalArgumentException) {
            return null
        }
        return saved.books.takeIf { saved.serverId == session.server.id && saved.userId == session.userId }
    }

    fun saveLibrary(session: Session, books: List<Book>) {
        writeText(libraryFile, JellyfinClient.json.encodeToString(SavedLibrary(session.server.id, session.userId, books)))
    }

    /** Where the user stopped reading this book on this device, or null if never opened (or unreadable). */
    fun readPosition(session: Session, bookId: String): ReadingPosition? {
        val file = positionFile(session, bookId)
        if (!SystemFileSystem.exists(file)) return null
        return try {
            JellyfinClient.json.decodeFromString<ReadingPosition>(readText(file))
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Saves a newly read [position] (not synced yet). Keeps what Jellyfin had at the last sync.
     * Whether the book is finished comes with [position]: paging back from the end undoes it.
     * Call with [kotlinx.coroutines.CoroutineStart.UNDISPATCHED] to keep the order of rapid page turns.
     */
    suspend fun savePosition(session: Session, bookId: String, position: ReadingPosition) = positionLock.withLock {
        withContext(Dispatchers.IO) {
            SystemFileSystem.createDirectories(positionsDir(session))
            val previous = readPosition(session, bookId)
            val saved = position.copy(
                isSynced = false,
                serverTicks = previous?.serverTicks,
            )
            writeText(positionFile(session, bookId), JellyfinClient.json.encodeToString(saved))
        }
    }

    /**
     * The last page is shown: the current position counts as finished. Call like [savePosition];
     * the next saved position decides for itself again.
     */
    suspend fun markFinished(session: Session, bookId: String) = positionLock.withLock {
        withContext(Dispatchers.IO) {
            val position = readPosition(session, bookId)
            if (position != null && !position.isFinished) {
                val finished = position.copy(isFinished = true, isSynced = false)
                writeText(positionFile(session, bookId), JellyfinClient.json.encodeToString(finished))
            }
        }
    }

    /** Every book's position on this device for this user, by book id. */
    fun readAllPositions(session: Session): Map<String, ReadingPosition> {
        val dir = positionsDir(session)
        if (!SystemFileSystem.exists(dir)) return emptyMap()
        return SystemFileSystem.list(dir)
            .filter { it.name.endsWith(".json") }
            .mapNotNull { file ->
                file.name.removeSuffix(".json").let { id -> readPosition(session, id)?.let { id to it } }
            }
            .toMap()
    }

    /** Like [readPosition], but only after the saves requested before this call are written. */
    suspend fun currentPosition(session: Session, bookId: String): ReadingPosition? = positionLock.withLock {
        withContext(Dispatchers.IO) { readPosition(session, bookId) }
    }

    /** Like [readAllPositions], but only after the saves requested before this call are written. */
    suspend fun currentPositions(session: Session): Map<String, ReadingPosition> = positionLock.withLock {
        withContext(Dispatchers.IO) { readAllPositions(session) }
    }

    /**
     * Records that [position] reached the server as [serverTicks]. Skipped if the book was read
     * further meanwhile: that newer position still has to be sent.
     */
    suspend fun markSynced(
        session: Session,
        bookId: String,
        position: ReadingPosition,
        serverTicks: Long,
    ) = positionLock.withLock {
        withContext(Dispatchers.IO) {
            if (readPosition(session, bookId) == position) {
                val synced = position.copy(isSynced = true, serverTicks = serverTicks)
                writeText(positionFile(session, bookId), JellyfinClient.json.encodeToString(synced))
            }
        }
    }

    /** Books whose last position hasn't reached the server yet, e.g. because they were read offline. */
    fun unsyncedBookIds(session: Session): List<String> =
        readAllPositions(session).filterValues { !it.isSynced }.keys.toList()

    private fun positionsDir(session: Session) = Path(positionsRoot, session.server.id, session.userId)

    private fun positionFile(session: Session, bookId: String) = Path(positionsDir(session), "$bookId.json")

    private fun readText(file: Path) = SystemFileSystem.source(file).buffered().use { it.readString() }

    /** Writes a new file, then replaces the old one, so a crash never leaves half a file behind. */
    private fun writeText(file: Path, text: String) {
        val temporary = Path("$file.tmp")
        SystemFileSystem.sink(temporary).buffered().use { it.writeString(text) }
        SystemFileSystem.atomicMove(temporary, file)
    }

    /** Saved with the user and server it belongs to, so another login never shows someone else's books. */
    @Serializable
    private class SavedLibrary(val serverId: String, val userId: String, val books: List<Book>)
}

/**
 * A place in a book. [locator] is the renderer's exact position (opaque to shared code); [progress]
 * is how far into the whole book it is, 0..1; [updatedAtMillis] is when it was read there;
 * [isSynced] is whether Jellyfin knows about it.
 *
 * [serverTicks] is the progress Jellyfin had after this device's last sync. If Jellyfin has a
 * different value later, the book was read elsewhere since (e.g. in Jellyfin's web reader).
 * [isFinished]: this position is at the end of the book: its last page, or 95 % of it.
 */
@Serializable
data class ReadingPosition(
    val locator: String,
    val progress: Double,
    val updatedAtMillis: Long,
    val isSynced: Boolean = false,
    val serverTicks: Long? = null,
    val isFinished: Boolean = false,
)
