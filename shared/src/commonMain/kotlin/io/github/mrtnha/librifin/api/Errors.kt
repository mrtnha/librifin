package io.github.mrtnha.librifin.api

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException

/** Turns a failed API call into a message that can be shown to the user as-is. */
fun Throwable.toUserMessage(): String = when (this) {
    is ClientRequestException -> when (response.status.value) {
        401 -> "Wrong username or password."
        403 -> "This account isn't allowed to sign in."
        404 -> "This doesn't look like a Jellyfin server (HTTP 404)."
        else -> "The server rejected the request (HTTP ${response.status.value})."
    }
    is ServerResponseException -> "The server had a problem (HTTP ${response.status.value}). Try again later."
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException ->
        "The server didn't respond in time."
    is SerializationException, is ContentConvertException -> "This doesn't look like a Jellyfin server."
    else -> "Couldn't reach the server. Check the address and your network connection."
}
