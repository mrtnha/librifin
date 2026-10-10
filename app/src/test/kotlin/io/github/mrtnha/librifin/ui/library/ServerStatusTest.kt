package io.github.mrtnha.librifin.ui.library

import kotlin.test.Test
import kotlin.test.assertEquals

class ServerStatusTest {

    @Test
    fun withoutANetworkTheServerCantBeAsked() {
        // Whatever the server answered before: it's the phone, not the server.
        assertEquals(ServerStatus.NO_NETWORK, serverStatus(hasNetwork = false, isServerReachable = true))
        assertEquals(ServerStatus.NO_NETWORK, serverStatus(hasNetwork = false, isServerReachable = false))
        assertEquals(ServerStatus.NO_NETWORK, serverStatus(hasNetwork = false, isServerReachable = null))
    }

    @Test
    fun withANetworkTheServersAnswerCounts() {
        assertEquals(ServerStatus.REACHABLE, serverStatus(hasNetwork = true, isServerReachable = true))
        assertEquals(ServerStatus.UNREACHABLE, serverStatus(hasNetwork = true, isServerReachable = false))
    }

    @Test
    fun unknownUntilTheServerAnswers() {
        assertEquals(ServerStatus.UNKNOWN, serverStatus(hasNetwork = true, isServerReachable = null))
    }
}
