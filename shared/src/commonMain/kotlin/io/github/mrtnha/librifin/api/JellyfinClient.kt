package io.github.mrtnha.librifin.api

import io.github.mrtnha.librifin.platform.Platform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Thin wrapper around the Jellyfin REST API. All calls take the server's base URL explicitly. */
class JellyfinClient(private val platform: Platform) {

    private val http = HttpClient {
        expectSuccess = true // non-2xx responses throw, see toUserMessage()
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 5_000
            requestTimeoutMillis = 15_000
        }
    }

    /** Checks that [baseUrl] points to a Jellyfin server. Doesn't need a login. */
    suspend fun publicSystemInfo(baseUrl: String): PublicSystemInfo =
        http.get("$baseUrl/System/Info/Public") { authorize(token = null) }.body()

    suspend fun authenticateByName(baseUrl: String, username: String, password: String): AuthenticationResult =
        http.post("$baseUrl/Users/AuthenticateByName") {
            authorize(token = null)
            contentType(ContentType.Application.Json)
            setBody(AuthenticateUserByName(username, password))
        }.body()

    fun close() = http.close()

    private fun HttpRequestBuilder.authorize(token: String?) {
        header(HttpHeaders.Authorization, authorizationHeader(token))
    }

    /** `MediaBrowser Client="…", Device="…", DeviceId="…", Version="…"[, Token="…"]`, values URL-encoded. */
    private fun authorizationHeader(token: String?): String {
        val params = buildList {
            add("Client" to CLIENT_NAME)
            add("Device" to platform.deviceName)
            add("DeviceId" to platform.deviceId)
            add("Version" to platform.appVersion)
            if (token != null) add("Token" to token)
        }
        return "MediaBrowser " + params.joinToString(", ") { (key, value) ->
            "$key=\"${value.replace(Regex("[\\r\\n]"), "").encodeURLParameter()}\""
        }
    }

    companion object {
        const val CLIENT_NAME = "Librifin"

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
