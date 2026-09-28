package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.BOOK_PROGRESS_TICKS
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.UserItemDataDto
import io.github.mrtnha.librifin.api.clientErrorStatus
import io.github.mrtnha.librifin.api.toUserMessage
import io.github.mrtnha.librifin.platform.SettingsStore
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.storage.ReadingPosition
import io.github.mrtnha.librifin.sync.ProgressSync
import io.ktor.http.HttpStatusCode
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

sealed interface ReaderState {
    /** [progress] is 0..1, or null while the size is unknown. */
    data class Downloading(val progress: Float?) : ReaderState

    /**
     * Where to open the book: at the exact [startLocator] saved on this device, else at
     * [startProgress] (0..1) from Jellyfin, else at the beginning.
     */
    data class Ready(val file: Path, val startLocator: String?, val startProgress: Double?) : ReaderState

    /**
     * [canRetry]: false if trying again won't help: the file arrived but isn't a readable book,
     * or the login has expired.
     */
    data class Error(val message: String, val canRetry: Boolean = true) : ReaderState
}

/**
 * Gets the book's EPUB onto the device. Opening a book is the download: the file is kept and reused
 * next time, so books that were read once open instantly, also without a connection.
 * Opens the book where it was left off, on this device or elsewhere (Jellyfin), whichever is newer.
 */
class ReaderViewModel(
    private val session: Session,
    private val bookId: String,
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
    private val progressSync: ProgressSync,
    private val settings: SettingsStore,
    /** Outlives this screen, so the last position is still saved when the reader is left right away. */
    private val appScope: CoroutineScope,
) : ViewModel() {
    var state by mutableStateOf<ReaderState>(ReaderState.Downloading(progress = null))
        private set

    /** App bar and system bars over the book. The book opens full screen; a tap in the middle toggles them. */
    var areBarsVisible by mutableStateOf(false)
        private set

    /** The page colors, the same for all books and kept between app starts. */
    var theme by mutableStateOf(
        ReaderTheme.entries.find { it.name == settings.read(KEY_THEME) } ?: ReaderTheme.DARK,
    )
        private set

    /** The text size in percent of the book's own, the same for all books and kept between app starts. */
    var fontSize by mutableStateOf(
        settings.read(KEY_FONT_SIZE)?.toIntOrNull()?.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE) ?: DEFAULT_FONT_SIZE,
    )
        private set

    val canDecreaseFontSize get() = fontSize > MIN_FONT_SIZE
    val canIncreaseFontSize get() = fontSize < MAX_FONT_SIZE

    /**
     * Set when the book was read further elsewhere while open here: the renderer jumps there (0..1)
     * and calls [onJumped].
     */
    var jumpToProgress by mutableStateOf<Double?>(null)
        private set

    /** The book's pages (see [BookPage]), empty until it's open. */
    var pages by mutableStateOf<List<BookPage>>(emptyList())
        private set

    /** The page shown now (1-based), null until known. */
    var currentPage by mutableStateOf<Int?>(null)
        private set

    private val file = bookStore.bookFile(bookId)
    private var lastLocator: String? = null
    private var serverCheck: Job? = null

    init {
        load()
    }

    fun load() {
        state = ReaderState.Downloading(progress = null)
        viewModelScope.launch {
            state = try {
                val local = withContext(Dispatchers.IO) {
                    downloadIfMissing()
                    bookStore.readPosition(session, bookId)
                }
                readyState(local, serverUserData())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // With an expired login, trying again fails the same way. The library offers to log in again.
                val isLoginExpired = e.clientErrorStatus == HttpStatusCode.Unauthorized
                ReaderState.Error(e.toDownloadMessage(), canRetry = !isLoginExpired)
            }
        }
    }

    /** Called on every page turn with the renderer's position and how far into the book it is. */
    fun onPositionChanged(locator: String, progress: Double, page: Int?) {
        currentPage = page
        // The renderer reports the same page again e.g. when the app comes back: that's no new reading,
        // and saving it would overwrite progress made elsewhere meanwhile.
        if (locator == lastLocator) return
        lastLocator = locator
        val position = ReadingPosition(
            locator,
            progress,
            Clock.System.now().toEpochMilliseconds(),
            isFinished = progress >= FINISHED_PROGRESS,
        )
        appScope.launch(start = CoroutineStart.UNDISPATCHED) { bookStore.savePosition(session, bookId, position) }
        progressSync.schedule(session, bookId)
    }

    /**
     * The last page of the book is shown: it counts as finished, here and in Jellyfin, also if that's
     * before [FINISHED_PROGRESS] (a very short book). Paging back makes it unfinished again.
     */
    fun onReachedEnd() {
        appScope.launch(start = CoroutineStart.UNDISPATCHED) { bookStore.markFinished(session, bookId) }
        progressSync.schedule(session, bookId)
    }

    /** Back in the app with the book open: maybe it was read further elsewhere meanwhile. */
    fun onAppResumed() {
        if (state !is ReaderState.Ready || serverCheck?.isActive == true) return
        serverCheck = viewModelScope.launch {
            val server = serverUserData() ?: return@launch
            jumpToProgress = serverProgressIfReadElsewhere(bookStore.currentPosition(session, bookId), server) ?: return@launch
        }
    }

    fun onPagesLoaded(pages: List<BookPage>) {
        this.pages = pages
    }

    /** Chosen with the page slider. */
    fun jumpToPage(page: Int) {
        val target = pages.getOrNull(page - 1) ?: return
        currentPage = page // Right away, so the slider doesn't spring back until the page is shown.
        jumpToProgress = target.progress
    }

    fun onJumped() {
        jumpToProgress = null
    }

    fun toggleBars() {
        areBarsVisible = !areBarsVisible
    }

    /** The eye button: the next theme. */
    fun cycleTheme() {
        theme = theme.next()
        settings.write(KEY_THEME, theme.name)
    }

    fun decreaseFontSize() = changeFontSize(fontSize - FONT_SIZE_STEP)

    fun increaseFontSize() = changeFontSize(fontSize + FONT_SIZE_STEP)

    private fun changeFontSize(percent: Int) {
        fontSize = percent.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        settings.write(KEY_FONT_SIZE, fontSize.toString())
    }

    /** The downloaded file isn't a book we can show. Deleted, so the next attempt gets a fresh copy. */
    fun onOpenFailed(message: String) {
        bookStore.deleteBook(bookId)
        state = ReaderState.Error(message, canRetry = false)
    }

    override fun onCleared() {
        progressSync.sendNow(session, bookId)
    }

    /** How far the book was read according to Jellyfin, or null if unknown or not reachable quickly. */
    private suspend fun serverUserData(): UserItemDataDto? =
        withTimeoutOrNull(SERVER_POSITION_TIMEOUT_MS) {
            try {
                jellyfin.userData(session, bookId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null // Offline: this device's position is all we have.
            }
        }

    /** Opens where the book was read last: the exact spot on this device, or Jellyfin's if it was read elsewhere. */
    private fun readyState(local: ReadingPosition?, server: UserItemDataDto?): ReaderState.Ready {
        val serverProgress = server?.let { serverProgressIfReadElsewhere(local, it) }
        return if (serverProgress != null) {
            ReaderState.Ready(file, startLocator = null, startProgress = serverProgress)
        } else {
            lastLocator = local?.locator
            ReaderState.Ready(file, startLocator = local?.locator, startProgress = null)
        }
    }

    /**
     * Jellyfin's progress (0..1) if the book was read further elsewhere than on this device, else null.
     *
     * Decided by value, not by time: Jellyfin's web reader sets the "last played" date only when a
     * book is opened, not while reading. So Jellyfin having another value than this device last sent
     * means someone else moved it. Progress made here that isn't sent yet (e.g. offline) wins.
     */
    private fun serverProgressIfReadElsewhere(local: ReadingPosition?, server: UserItemDataDto): Double? {
        // 0 means unknown: e.g. never read, or reset when a book is closed in some clients.
        val serverTicks = server.playbackPositionTicks?.takeIf { it > 0 } ?: return null
        if (local != null && (!local.isSynced || local.serverTicks == serverTicks)) return null
        return (serverTicks.toDouble() / BOOK_PROGRESS_TICKS).coerceIn(0.0, 1.0)
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
        when (clientErrorStatus) {
            HttpStatusCode.Forbidden ->
                "Your account isn't allowed to download books. An admin can turn on “Allow media downloads” " +
                    "for your user in the Jellyfin dashboard."
            HttpStatusCode.NotFound -> "This book isn't on the server anymore."
            else -> toUserMessage()
        }

    private companion object {
        /** Asking Jellyfin must not keep a downloaded book from opening offline for long. */
        const val SERVER_POSITION_TIMEOUT_MS = 2_000L

        /**
         * From here on a book counts as finished, even if its last page is never shown: after the story
         * often come notes, acknowledgements or ads, which many readers skip. Paging back before it
         * makes the book unfinished again.
         */
        const val FINISHED_PROGRESS = 0.95

        const val KEY_THEME = "reader_theme"
        const val KEY_FONT_SIZE = "reader_font_size"

        // Readium allows 10–500 %; beyond these limits text on a phone is too small to read,
        // or only a few words fit on a line.
        const val DEFAULT_FONT_SIZE = 100
        const val MIN_FONT_SIZE = 70
        const val MAX_FONT_SIZE = 250
        const val FONT_SIZE_STEP = 10
    }
}
