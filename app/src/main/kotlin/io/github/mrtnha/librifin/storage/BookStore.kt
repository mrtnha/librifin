package io.github.mrtnha.librifin.storage

import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.decodeOrNull
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

    /** The books downloaded on this device, for every user who logged in here. */
    fun downloads(): Downloads {
        val books = filesInBooksDir().filter { it.name.endsWith(".epub") }
        val bytes = books.sumOf { SystemFileSystem.metadataOrNull(it)?.size ?: 0L }
        return Downloads(bookCount = books.size, bytes = bytes)
    }

    /** Deletes every downloaded book and unfinished download. Positions and the saved book list stay. */
    fun deleteAllBooks() = filesInBooksDir().forEach { SystemFileSystem.delete(it, mustExist = false) }

    private fun filesInBooksDir(): Collection<Path> =
        if (SystemFileSystem.exists(booksDir)) SystemFileSystem.list(booksDir) else emptyList()

    /** The book list saved for this user on this server, or null if there is none (or it's unreadable). */
    fun readLibrary(session: Session): List<Book>? =
        readJson<SavedLibrary>(libraryFile)
            ?.takeIf { it.serverId == session.server.id && it.userId == session.userId }
            ?.books

    fun saveLibrary(session: Session, books: List<Book>) =
        writeJson(libraryFile, SavedLibrary(session.server.id, session.userId, books))

    /** Where the user stopped reading this book on this device, or null if never opened (or unreadable). */
    fun readPosition(session: Session, bookId: String): ReadingPosition? = readJson(positionFile(session, bookId))

    /**
     * Saves a newly read [position] (not synced yet). Keeps what Jellyfin had at the last sync.
     * Whether the book is finished comes with [position]: paging back from the end undoes it.
     * Call with [kotlinx.coroutines.CoroutineStart.UNDISPATCHED] to keep the order of rapid page turns.
     */
    suspend fun savePosition(session: Session, bookId: String, position: ReadingPosition) =
        updatePosition(session, bookId) { saved -> position.copy(isSynced = false, serverTicks = saved?.serverTicks) }

    /**
     * The last page is shown: the current position counts as finished. Call like [savePosition];
     * the next saved position decides for itself again.
     */
    suspend fun markFinished(session: Session, bookId: String) =
        updatePosition(session, bookId) { saved ->
            if (saved != null && !saved.isFinished) saved.copy(isFinished = true, isSynced = false) else null
        }

    /** Every book's position on this device for this user, by book id. */
    fun readAllPositions(session: Session): Map<String, ReadingPosition> {
        val dir = positionsDir(session)
        if (!SystemFileSystem.exists(dir)) return emptyMap()
        return SystemFileSystem.list(dir)
            .filter { it.name.endsWith(".json") }
            .mapNotNull { file -> readJson<ReadingPosition>(file)?.let { file.name.removeSuffix(".json") to it } }
            .toMap()
    }

    /** Like [readPosition], but only after the saves requested before this call are written. */
    suspend fun currentPosition(session: Session, bookId: String): ReadingPosition? =
        inTurn { readPosition(session, bookId) }

    /** Like [readAllPositions], but only after the saves requested before this call are written. */
    suspend fun currentPositions(session: Session): Map<String, ReadingPosition> =
        inTurn { readAllPositions(session) }

    /**
     * Records that [position] reached the server as [serverTicks]. Skipped if the book was read
     * further meanwhile: that newer position still has to be sent.
     */
    suspend fun markSynced(session: Session, bookId: String, position: ReadingPosition, serverTicks: Long) =
        updatePosition(session, bookId) { saved ->
            if (saved == position) position.copy(isSynced = true, serverTicks = serverTicks) else null
        }

    /** Books whose last position hasn't reached the server yet, e.g. because they were read offline. */
    fun unsyncedBookIds(session: Session): List<String> =
        readAllPositions(session).filterValues { !it.isSynced }.keys.toList()

    /** Saves what [change] makes of the saved position (null: leaves it as it is), [inTurn] with the other saves. */
    private suspend fun updatePosition(
        session: Session,
        bookId: String,
        change: (saved: ReadingPosition?) -> ReadingPosition?,
    ) = inTurn {
        val changed = change(readPosition(session, bookId)) ?: return@inTurn
        SystemFileSystem.createDirectories(positionsDir(session))
        writeJson(positionFile(session, bookId), changed)
    }

    /** Runs [block] on the IO threads after the saves requested before this call, and before later ones. */
    private suspend fun <T> inTurn(block: () -> T): T =
        positionLock.withLock { withContext(Dispatchers.IO) { block() } }

    private fun positionsDir(session: Session) = Path(positionsRoot, session.server.id, session.userId)

    private fun positionFile(session: Session, bookId: String) = Path(positionsDir(session), "$bookId.json")

    /** [file] read as [T], or null if there is no such file or it isn't a valid [T]. */
    private inline fun <reified T> readJson(file: Path): T? =
        if (SystemFileSystem.exists(file)) JellyfinClient.json.decodeOrNull<T>(readText(file)) else null

    private inline fun <reified T> writeJson(file: Path, value: T) =
        writeText(file, JellyfinClient.json.encodeToString(value))

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

/** [bookCount] downloaded books, which take [bytes] together. */
data class Downloads(val bookCount: Int, val bytes: Long)

/**
 * A place in a book. [locator] is the renderer's exact position (only it understands it); [progress]
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
