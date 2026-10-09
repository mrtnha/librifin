package io.github.mrtnha.librifin.api

import kotlinx.serialization.Serializable

/** A verified Jellyfin server, ready to log in to. */
@Serializable
data class Server(
    val baseUrl: String,
    val id: String,
    val name: String,
    val version: String?,
)

/** A logged-in user on a server. Saved between app starts via [io.github.mrtnha.librifin.platform.SessionStore]. */
@Serializable
data class Session(
    val server: Server,
    val userId: String,
    val userName: String,
    /** Tag of the user's profile picture, null if there is none. */
    val userImageTag: String?,
    val accessToken: String,
)
