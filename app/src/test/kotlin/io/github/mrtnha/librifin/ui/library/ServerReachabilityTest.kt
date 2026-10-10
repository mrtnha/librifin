package io.github.mrtnha.librifin.ui.library

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerReachabilityTest {

    @Test
    fun notKnownBeforeTheFirstAnswer() {
        assertNull(whileChecking(lastAnswer = null, ageMillis = 0))
    }

    @Test
    fun aRecentAnswerStaysWhileTheServerIsAskedAgain() {
        assertEquals(true, whileChecking(lastAnswer = true, ageMillis = 30_000))
        assertEquals(false, whileChecking(lastAnswer = false, ageMillis = 30_000))
    }

    @Test
    fun anAnswerCountsForAMinute() {
        assertEquals(true, whileChecking(lastAnswer = true, ageMillis = 60_000))
        assertNull(whileChecking(lastAnswer = true, ageMillis = 60_001))
    }

    @Test
    fun anOldAnswerNoLongerCounts() {
        // E.g. back in the app after a while: the phone may have left the server's network meanwhile.
        assertNull(whileChecking(lastAnswer = true, ageMillis = 10 * 60_000))
        assertNull(whileChecking(lastAnswer = false, ageMillis = 10 * 60_000))
    }

    private fun whileChecking(lastAnswer: Boolean?, ageMillis: Long) =
        reachabilityWhileChecking(lastAnswer, answeredAtMillis = NOW - ageMillis, nowMillis = NOW)

    private companion object {
        const val NOW = 1_000_000_000L
    }
}
