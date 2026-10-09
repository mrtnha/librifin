package io.github.mrtnha.librifin.sync

import io.github.mrtnha.librifin.api.BOOK_PROGRESS_TICKS
import io.github.mrtnha.librifin.api.UserItemDataDto
import io.github.mrtnha.librifin.storage.ReadingPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgressSyncTest {
    private val atHalf = UserItemDataDto(playbackPositionTicks = BOOK_PROGRESS_TICKS / 2)

    /** Read to 30 % here; [serverTicks] is what Jellyfin had after this device's last sync. */
    private fun local(isSynced: Boolean, serverTicks: Long? = null) =
        ReadingPosition(locator = "{}", progress = 0.3, updatedAtMillis = 1, isSynced = isSynced, serverTicks = serverTicks)

    @Test
    fun noProgressOnTheServerIsIgnored() {
        assertNull(serverProgressIfReadElsewhere(null, UserItemDataDto(playbackPositionTicks = null)))
        assertNull(serverProgressIfReadElsewhere(null, UserItemDataDto(playbackPositionTicks = 0)))
    }

    @Test
    fun neverOpenedHereTakesTheServerProgress() =
        assertEquals(0.5, serverProgressIfReadElsewhere(null, atHalf))

    @Test
    fun unsyncedProgressFromHereWins() =
        assertNull(serverProgressIfReadElsewhere(local(isSynced = false, serverTicks = 1), atHalf))

    @Test
    fun serverStillHasWhatThisDeviceSent() =
        assertNull(serverProgressIfReadElsewhere(local(isSynced = true, serverTicks = BOOK_PROGRESS_TICKS / 2), atHalf))

    @Test
    fun serverChangedSinceTheLastSyncMeansReadElsewhere() =
        assertEquals(0.5, serverProgressIfReadElsewhere(local(isSynced = true, serverTicks = BOOK_PROGRESS_TICKS / 4), atHalf))

    @Test
    fun progressBeyondTheEndIsClamped() =
        assertEquals(1.0, serverProgressIfReadElsewhere(null, UserItemDataDto(playbackPositionTicks = BOOK_PROGRESS_TICKS * 2)))
}
