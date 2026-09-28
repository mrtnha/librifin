package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.toUserMessage
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
    data class Error(val message: String) : ReaderState
}

/**
 * Gets the book's EPUB onto the device. Opening a book is the download: the file is kept in the cache
 * and reused next time, so books that were read once open instantly.
 */
class ReaderViewModel(
    private val session: Session,
    private val bookId: String,
    private val jellyfin: JellyfinClient,
    cacheDir: String,
) : ViewModel() {
    var state by mutableStateOf<ReaderState>(ReaderState.Downloading(progress = null))
        private set

    private val booksDir = Path(cacheDir, "books")
    private val file = Path(booksDir, "$bookId.epub")

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

    /** Downloads to a temporary file first, so an interrupted download is never mistaken for the book. */
    private suspend fun downloadIfMissing() {
        if (SystemFileSystem.exists(file)) return
        SystemFileSystem.createDirectories(booksDir)
        val partial = Path(booksDir, "$bookId.epub.part")
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
