package io.github.mrtnha.librifin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Only the fields Librifin uses. Names and types follow docs/openapi.json.

@Serializable
data class PublicSystemInfo(
    @SerialName("Id") val id: String? = null,
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
    @SerialName("LocalAddress") val localAddress: String? = null,
)

@Serializable
data class AuthenticateUserByName(
    @SerialName("Username") val username: String,
    @SerialName("Pw") val password: String,
)

@Serializable
data class AuthenticationResult(
    @SerialName("User") val user: UserDto? = null,
    @SerialName("AccessToken") val accessToken: String? = null,
    @SerialName("ServerId") val serverId: String? = null,
)

@Serializable
data class UserDto(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String? = null,
)

/** Reply to a UDP discovery broadcast. */
@Serializable
data class DiscoveryResponse(
    @SerialName("Id") val id: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("Address") val address: String? = null,
    @SerialName("EndpointAddress") val endpointAddress: String? = null,
)
