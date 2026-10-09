package io.github.mrtnha.librifin.storage

import kotlinx.serialization.Serializable

/**
 * One book of the library, as the grid shows it and as [BookStore] saves it on the device.
 * [serverProgress] (0..1) and [isPlayed] are Jellyfin's view of how far it was read, [lastPlayedMillis]
 * when Jellyfin last saw it read (on any device; null if never, or in lists saved before it was loaded).
 * [authors] is empty in lists saved before authors were loaded.
 */
@Serializable
data class Book(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val serverProgress: Double? = null,
    val isPlayed: Boolean = false,
    val authors: List<String> = emptyList(),
    val lastPlayedMillis: Long? = null,
) {
    /** Every word of [query] is in the title or an author's name, ignoring case: "tolkien hobbit" finds the book. */
    fun matches(query: String): Boolean =
        query.trim().split(Regex("\\s+")).all { word ->
            title.contains(word, ignoreCase = true) || authors.any { it.contains(word, ignoreCase = true) }
        }
}
