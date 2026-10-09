package io.github.mrtnha.librifin.api

import io.github.mrtnha.librifin.platform.Platform
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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

    /** The user's libraries (movies, music, books, …). */
    suspend fun userViews(session: Session): List<BaseItemDto> =
        http.get("${session.server.baseUrl}/UserViews") {
            authorize(session.accessToken)
            parameter("userId", session.userId)
        }.body<BaseItemDtoQueryResult>().items

    /** All books in a library, by title. The library screen sorts them again, in the order the user chose. */
    suspend fun books(session: Session, libraryId: String): List<BaseItemDto> =
        http.get("${session.server.baseUrl}/Items") {
            authorize(session.accessToken)
            parameter("userId", session.userId)
            parameter("parentId", libraryId)
            parameter("includeItemTypes", "Book")
            parameter("recursive", true)
            parameter("sortBy", "SortName")
            parameter("fields", "People,SortName,DateCreated")
        }.body<BaseItemDtoQueryResult>().items

    /**
     * Downloads the original file of a book to [target], streamed in chunks so large books never sit in memory.
     * [onProgress] gets the bytes written so far and the total size, if the server sends it.
     */
    suspend fun downloadBook(
        session: Session,
        itemId: String,
        target: Path,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
    ) {
        http.prepareGet("${session.server.baseUrl}/Items/$itemId/Download") {
            authorize(session.accessToken)
            // Big books take longer than the usual request timeout; only give up if the connection stalls.
            timeout {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = 15_000
            }
        }.execute { response ->
            val totalBytes = response.contentLength()
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
            var bytesRead = 0L
            SystemFileSystem.sink(target).buffered().use { sink ->
                while (true) {
                    val count = channel.readAvailable(buffer)
                    if (count == -1) break
                    sink.write(buffer, 0, count)
                    bytesRead += count
                    onProgress(bytesRead, totalBytes)
                }
            }
        }
    }

    /** The user's state of one item, e.g. how far a book has been read. */
    suspend fun userData(session: Session, itemId: String): UserItemDataDto =
        http.get("${session.server.baseUrl}/UserItems/$itemId/UserData") {
            authorize(session.accessToken)
            parameter("userId", session.userId)
        }.body()

    /** Changes the fields of [update] that are set, e.g. the reading progress and when it was read. */
    suspend fun updateUserData(session: Session, itemId: String, update: UpdateUserItemDataDto) {
        http.post("${session.server.baseUrl}/UserItems/$itemId/UserData") {
            authorize(session.accessToken)
            parameter("userId", session.userId)
            contentType(ContentType.Application.Json)
            setBody(update)
        }
    }

    /** Cover URL, or null if the item has no cover. The tag changes when the image does, so it's safe to cache. */
    fun primaryImageUrl(baseUrl: String, item: BaseItemDto, maxWidth: Int): String? {
        val tag = item.imageTags?.get("Primary") ?: return null
        return "$baseUrl/Items/${item.id}/Images/Primary?tag=$tag&maxWidth=$maxWidth&quality=90"
    }

    /** Ends the session on the server, which invalidates the access token. */
    suspend fun logout(session: Session) {
        http.post("${session.server.baseUrl}/Sessions/Logout") { authorize(session.accessToken) }
    }

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
        private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}

/**
 * [text] decoded as [T], or null if it isn't a valid [T], e.g. a damaged file or an unexpected reply.
 * That includes a [kotlinx.serialization.SerializationException], which is an [IllegalArgumentException].
 */
inline fun <reified T> Json.decodeOrNull(text: String): T? =
    try {
        decodeFromString<T>(text)
    } catch (_: IllegalArgumentException) {
        null
    }
