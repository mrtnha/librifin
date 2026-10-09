package io.github.mrtnha.librifin.ui.library

import io.github.mrtnha.librifin.storage.Book
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LibrarySortTest {

    @Test
    fun titleSortsAsJellyfinDoes() {
        val books = listOf(
            book("Walden", sortTitle = "walden"),
            book("The Prince", sortTitle = "prince"),
            book("Buddenbrooks", sortTitle = "buddenbrooks"),
        )

        assertEquals(listOf("Buddenbrooks", "The Prince", "Walden"), sorted(books, LibrarySort.TITLE))
    }

    @Test
    fun withoutSortTitleTheShownTitleStandsIn() {
        val books = listOf(book("The Prince"), book("Robinson Crusoe"))

        assertEquals(listOf("Robinson Crusoe", "The Prince"), sorted(books, LibrarySort.TITLE))
    }

    @Test
    fun accentedTitlesSortWithTheirLetter() {
        val books = listOf(book("Zarathustra"), book("Peter Pan"), book("Oliver Twist"), book("Ödipus"))

        val titles = inLocale(Locale.ENGLISH) { sorted(books, LibrarySort.TITLE) }

        assertEquals(listOf("Ödipus", "Oliver Twist", "Peter Pan", "Zarathustra"), titles)
    }

    @Test
    fun recentlyReadFirstNewestFirstThenTheOthersByTitle() {
        val books = listOf(book("Walden"), book("Buddenbrooks"), book("The Prince"), book("Short Fiction"))
        val lastRead = mapOf("The Prince" to 200L, "Short Fiction" to 100L)

        assertEquals(
            listOf("The Prince", "Short Fiction", "Buddenbrooks", "Walden"),
            sorted(books, LibrarySort.RECENTLY_READ, lastRead),
        )
    }

    @Test
    fun dateAddedNewestFirstEqualDatesByTitleUndatedLast() {
        val books = listOf(
            book("Walden", dateAddedMillis = 100),
            book("Buddenbrooks"),
            book("Short Fiction", dateAddedMillis = 100),
            book("The Prince", dateAddedMillis = 300),
        )

        assertEquals(
            listOf("The Prince", "Short Fiction", "Walden", "Buddenbrooks"),
            sorted(books, LibrarySort.DATE_ADDED),
        )
    }

    /** The book's id is its title, so tests can refer to books by title. */
    private fun book(title: String, sortTitle: String? = null, dateAddedMillis: Long? = null) =
        Book(id = title, title = title, coverUrl = null, sortTitle = sortTitle, dateAddedMillis = dateAddedMillis)

    private fun sorted(books: List<Book>, sort: LibrarySort, lastRead: Map<String, Long> = emptyMap()) =
        sortBooks(books, sort, lastRead).map { it.title }

    /** Runs [block] with [locale] as the default, as the order of letters depends on the language. */
    private fun <T> inLocale(locale: Locale, block: () -> T): T {
        val default = Locale.getDefault()
        Locale.setDefault(locale)
        try {
            return block()
        } finally {
            Locale.setDefault(default)
        }
    }
}
