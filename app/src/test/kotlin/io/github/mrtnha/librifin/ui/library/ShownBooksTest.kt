package io.github.mrtnha.librifin.ui.library

import io.github.mrtnha.librifin.storage.Book
import kotlin.test.Test
import kotlin.test.assertEquals

class ShownBooksTest {

    private val walden = book("walden", "Walden")
    private val prince = book("prince", "The Prince")
    private val crusoe = book("crusoe", "Robinson Crusoe")

    private val library = LibraryState.Loaded(
        books = listOf(walden, prince, crusoe),
        progress = emptyMap(),
        downloadedIds = setOf("crusoe", "walden"),
    )

    @Test
    fun allBooksByDefault() {
        assertEquals(listOf(walden, prince, crusoe), library.shownBooks(downloadedOnly = false))
    }

    @Test
    fun downloadedOnlyKeepsTheDownloadedBooksInTheirOrder() {
        assertEquals(listOf(walden, crusoe), library.shownBooks(downloadedOnly = true))
    }

    @Test
    fun downloadedOnlyWithoutDownloadsShowsNothing() {
        assertEquals(emptyList(), library.copy(downloadedIds = emptySet()).shownBooks(downloadedOnly = true))
    }

    private fun book(id: String, title: String) = Book(id = id, title = title, coverUrl = null)
}
