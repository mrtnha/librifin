package io.github.mrtnha.librifin.storage

import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.ui.library.Book
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * What Librifin keeps on the device: the downloaded books, and the last loaded book list so the
 * library shows up instantly and while the server can't be reached. Blocking file I/O: call it off
 * the main thread.
 */
class BookStore(filesDir: String) {
    private val booksDir = Path(filesDir, "books")
    private val libraryFile = Path(filesDir, "library.json")

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
            val text = SystemFileSystem.source(libraryFile).buffered().use { it.readString() }
            JellyfinClient.json.decodeFromString<SavedLibrary>(text)
        } catch (_: SerializationException) {
            return null
        } catch (_: IllegalArgumentException) {
            return null
        }
        return saved.books.takeIf { saved.serverId == session.server.id && saved.userId == session.userId }
    }

    fun saveLibrary(session: Session, books: List<Book>) {
        val text = JellyfinClient.json.encodeToString(SavedLibrary(session.server.id, session.userId, books))
        // Write a new file, then replace the old one, so a crash never leaves half a list behind.
        val temporary = Path(libraryFile.toString() + ".tmp")
        SystemFileSystem.sink(temporary).buffered().use { it.writeString(text) }
        SystemFileSystem.atomicMove(temporary, libraryFile)
    }

    /** Saved with the user and server it belongs to, so another login never shows someone else's books. */
    @Serializable
    private class SavedLibrary(val serverId: String, val userId: String, val books: List<Book>)
}
