package io.github.mrtnha.librifin.api

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException

/**
 * The status the server refused the request with (4xx), or null if it failed another way.
 * [HttpStatusCode.Unauthorized] means the server doesn't accept the login: a wrong password at login,
 * an expired or revoked token otherwise.
 */
val Throwable.clientErrorStatus: HttpStatusCode?
    get() = (this as? ClientRequestException)?.response?.status

/**
 * Turns a failed API call into a message that can be shown to the user as-is. Screens where a status
 * means something specific say so themselves, e.g. 401 at login is a wrong password.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized -> "Your session has expired. Please log in again."
        else -> "The server rejected the request (HTTP ${response.status.value})."
    }
    is ServerResponseException -> "The server had a problem (HTTP ${response.status.value}). Try again later."
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException ->
        "The server didn't respond in time."
    is SerializationException, is ContentConvertException -> "This doesn't look like a Jellyfin server."
    else -> "Couldn't reach the server. Check the address and your network connection."
}
