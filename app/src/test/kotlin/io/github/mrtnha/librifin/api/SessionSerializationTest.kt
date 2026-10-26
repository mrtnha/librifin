package io.github.mrtnha.librifin.api

import kotlin.test.Test
import kotlin.test.assertEquals

class SessionSerializationTest {
    private val server = Server(baseUrl = "http://192.168.1.10:8096", id = "abc", name = "home")

    @Test
    fun sessionSurvivesRoundTrip() {
        val session = Session(server, userId = "u1", userName = "admin", accessToken = "t0k3n")
        val json = JellyfinClient.json.encodeToString(session)
        assertEquals(session, JellyfinClient.json.decodeFromString<Session>(json))
    }
}
