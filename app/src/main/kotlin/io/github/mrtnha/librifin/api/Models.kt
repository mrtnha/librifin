package io.github.mrtnha.librifin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Only the fields Librifin uses. Names and types follow docs/openapi.json.

@Serializable
data class PublicSystemInfo(
    @SerialName("Id") val id: String? = null,
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
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
)

@Serializable
data class UserDto(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String? = null,
)

/** A library (from /UserViews) or a book (from /Items). */
@Serializable
data class BaseItemDto(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String? = null,
    /** Only when requested with the "SortName" field. The name as Jellyfin sorts it, e.g. "prince" for "The Prince". */
    @SerialName("SortName") val sortName: String? = null,
    /** Only when requested with the "DateCreated" field. When the item was added to the library, ISO 8601. */
    @SerialName("DateCreated") val dateCreated: String? = null,
    @SerialName("CollectionType") val collectionType: String? = null,
    @SerialName("ImageTags") val imageTags: Map<String, String?>? = null,
    @SerialName("UserData") val userData: UserItemDataDto? = null,
    /** Only when requested with the "People" field. A book's authors have [BaseItemPerson.type] "Author". */
    @SerialName("People") val people: List<BaseItemPerson>? = null,
)

@Serializable
data class BaseItemPerson(
    @SerialName("Name") val name: String? = null,
    @SerialName("Type") val type: String? = null,
)

/**
 * The user's state of an item. For books, Jellyfin's own web reader stores the reading progress in
 * [playbackPositionTicks] as a fraction of [BOOK_PROGRESS_TICKS]; Librifin does the same.
 */
@Serializable
data class UserItemDataDto(
    @SerialName("PlaybackPositionTicks") val playbackPositionTicks: Long? = null,
    /** ISO 8601, e.g. `2026-09-28T14:03:12.1234567Z`. */
    @SerialName("LastPlayedDate") val lastPlayedDate: String? = null,
    @SerialName("Played") val played: Boolean? = null,
)

/** Only the given fields are changed on the server. */
@Serializable
data class UpdateUserItemDataDto(
    @SerialName("PlaybackPositionTicks") val playbackPositionTicks: Long? = null,
    @SerialName("LastPlayedDate") val lastPlayedDate: String? = null,
    @SerialName("Played") val played: Boolean? = null,
)

/** Reading progress 100 % in [UserItemDataDto.playbackPositionTicks], as in Jellyfin's web reader. */
const val BOOK_PROGRESS_TICKS = 10_000_000L

@Serializable
data class BaseItemDtoQueryResult(
    @SerialName("Items") val items: List<BaseItemDto> = emptyList(),
)

/** Reply to a UDP discovery broadcast. */
@Serializable
data class DiscoveryResponse(
    @SerialName("Id") val id: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("Address") val address: String? = null,
)
