package io.github.mrtnha.librifin.api

/** A verified Jellyfin server, ready to log in to. */
data class Server(
    val baseUrl: String,
    val id: String,
    val name: String,
    val version: String?,
)

/** A logged-in user on a server. Kept in memory only (persisting the login is out of scope for now). */
data class Session(
    val server: Server,
    val userId: String,
    val userName: String,
    /** Tag of the user's profile picture, null if there is none. */
    val userImageTag: String?,
    val accessToken: String,
)
