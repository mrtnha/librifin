package io.github.mrtnha.librifin.api

import kotlin.test.Test
import kotlin.test.assertEquals

class SessionSerializationTest {
    private val server = Server(baseUrl = "http://192.168.1.10:8096", id = "abc", name = "home", version = null)

    @Test
    fun sessionSurvivesRoundTrip() {
        val session = Session(server, userId = "u1", userName = "admin", accessToken = "t0k3n")
        val json = JellyfinClient.json.encodeToString(session)
        assertEquals(session, JellyfinClient.json.decodeFromString<Session>(json))
    }

    @Test
    fun sessionSavedWithProfilePictureTagStillLoads() {
        // Sessions saved while the app showed the profile picture also have its tag; it's ignored now.
        val saved = """{"server":{"baseUrl":"http://192.168.1.10:8096","id":"abc","name":"home"},""" +
            """"userId":"u1","userName":"admin","userImageTag":"tag1","accessToken":"t0k3n"}"""
        val session = Session(server, userId = "u1", userName = "admin", accessToken = "t0k3n")
        assertEquals(session, JellyfinClient.json.decodeFromString<Session>(saved))
    }
}
