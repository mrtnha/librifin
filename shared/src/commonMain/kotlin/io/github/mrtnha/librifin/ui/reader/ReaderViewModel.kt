package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.toUserMessage
import io.github.mrtnha.librifin.storage.BookStore
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

sealed interface ReaderState {
    /** [progress] is 0..1, or null while the size is unknown. */
    data class Downloading(val progress: Float?) : ReaderState
    data class Ready(val file: Path) : ReaderState
    /** [canRetry]: false if the file arrived but isn't a readable book, so downloading again won't help. */
    data class Error(val message: String, val canRetry: Boolean = true) : ReaderState
}

/**
 * Gets the book's EPUB onto the device. Opening a book is the download: the file is kept and reused
 * next time, so books that were read once open instantly, also without a connection.
 */
class ReaderViewModel(
    private val session: Session,
    private val bookId: String,
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
) : ViewModel() {
    var state by mutableStateOf<ReaderState>(ReaderState.Downloading(progress = null))
        private set

    /** App bar and system bars over the book. The book opens full screen; a tap in the middle toggles them. */
    var areBarsVisible by mutableStateOf(false)
        private set

    private val file = bookStore.bookFile(bookId)

    init {
        load()
    }

    fun load() {
        state = ReaderState.Downloading(progress = null)
        viewModelScope.launch {
            state = try {
                withContext(Dispatchers.IO) { downloadIfMissing() }
                ReaderState.Ready(file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ReaderState.Error(e.toDownloadMessage())
            }
        }
    }

    fun toggleBars() {
        areBarsVisible = !areBarsVisible
    }

    /** The downloaded file isn't a book we can show. Deleted, so the next attempt gets a fresh copy. */
    fun onOpenFailed(message: String) {
        bookStore.deleteBook(bookId)
        state = ReaderState.Error(message, canRetry = false)
    }

    /** Downloads to a temporary file first, so an interrupted download is never mistaken for the book. */
    private suspend fun downloadIfMissing() {
        if (bookStore.isDownloaded(bookId)) return
        val partial = bookStore.partialBookFile(bookId)
        try {
            var lastPercent = -1
            jellyfin.downloadBook(session, bookId, partial) { bytesRead, totalBytes ->
                if (totalBytes != null && totalBytes > 0) {
                    val percent = (bytesRead * 100 / totalBytes).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        state = ReaderState.Downloading(progress = percent / 100f)
                    }
                }
            }
            SystemFileSystem.atomicMove(partial, file)
        } finally {
            SystemFileSystem.delete(partial, mustExist = false)
        }
    }

    private fun Exception.toDownloadMessage(): String =
        when ((this as? ClientRequestException)?.response?.status?.value) {
            403 -> "Your account isn't allowed to download books. An admin can allow it in the Jellyfin user settings."
            404 -> "This book isn't on the server anymore."
            else -> toUserMessage()
        }
}
