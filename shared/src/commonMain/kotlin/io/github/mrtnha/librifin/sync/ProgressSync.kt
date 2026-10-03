package io.github.mrtnha.librifin.sync

import io.github.mrtnha.librifin.api.BOOK_PROGRESS_TICKS
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.UpdateUserItemDataDto
import io.github.mrtnha.librifin.api.UserItemDataDto
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.storage.ReadingPosition
import kotlin.math.roundToLong
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sends the reading progress to Jellyfin: once page turning pauses for a moment, right away when a
 * book is closed, and later for progress made offline. Uses the format of Jellyfin's web reader, so
 * each continues where the other left off. Call from the main thread.
 *
 * Known limitation: sync with the server doesn't fully work yet. Progress made in Librifin reaches
 * Jellyfin, but progress made in other clients (e.g. Jellyfin's web reader) isn't reliably picked up
 * (see [serverProgressIfReadElsewhere]). Needs a more thorough look: how other clients
 * report book progress (e.g. /Sessions/Playing/Progress, "last played" is only set when a book is
 * opened) and when Librifin should ask the server.
 */
class ProgressSync(
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
    /** Outlives the reader, so progress is still sent after the book was closed. */
    private val scope: CoroutineScope,
) {
    private val scheduled = mutableMapOf<String, Job>()

    /** A page was turned: send it once reading pauses, instead of on every page. */
    fun schedule(session: Session, bookId: String) {
        scheduled.remove(bookId)?.cancel()
        scheduled[bookId] = scope.launch {
            delay(QUIET_PERIOD_MS)
            send(session, bookId)
        }
    }

    /** The book was closed: send its last position right away. */
    fun sendNow(session: Session, bookId: String) {
        scheduled.remove(bookId)?.cancel()
        // Undispatched, so it waits for the page turns saved just before (see BookStore.savePosition).
        scope.launch(start = CoroutineStart.UNDISPATCHED) { send(session, bookId) }
    }

    /** Sends the progress that couldn't be sent before, e.g. because the server wasn't reachable. */
    fun sendUnsynced(session: Session) {
        scope.launch {
            withContext(Dispatchers.IO) { bookStore.unsyncedBookIds(session) }.forEach { send(session, it) }
        }
    }

    private suspend fun send(session: Session, bookId: String) {
        val position = bookStore.currentPosition(session, bookId) ?: return
        if (position.isSynced) return
        val ticks = (position.progress * BOOK_PROGRESS_TICKS).roundToLong()
        try {
            jellyfin.updateUserData(
                session,
                bookId,
                UpdateUserItemDataDto(
                    playbackPositionTicks = ticks,
                    lastPlayedDate = Instant.fromEpochMilliseconds(position.updatedAtMillis).toString(),
                    // Shown as "played" in Jellyfin. Also cleared, when paging back from the end.
                    played = position.isFinished,
                ),
            )
            bookStore.markSynced(session, bookId, position, serverTicks = ticks)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Not reachable right now: the position stays unsynced and is sent after the next library load.
        }
    }

    private companion object {
        const val QUIET_PERIOD_MS = 5_000L
    }
}

/**
 * Jellyfin's progress (0..1) if the book was read further elsewhere than on this device, else null.
 *
 * Decided by value, not by time: Jellyfin's web reader sets the "last played" date only when a
 * book is opened, not while reading. So Jellyfin having another value than this device last sent
 * means someone else moved it. Progress made here that isn't sent yet (e.g. offline) wins.
 */
fun serverProgressIfReadElsewhere(local: ReadingPosition?, server: UserItemDataDto): Double? {
    // 0 means unknown: e.g. never read, or reset when a book is closed in some clients.
    val serverTicks = server.playbackPositionTicks?.takeIf { it > 0 } ?: return null
    if (local != null && (!local.isSynced || local.serverTicks == serverTicks)) return null
    return (serverTicks.toDouble() / BOOK_PROGRESS_TICKS).coerceIn(0.0, 1.0)
}
