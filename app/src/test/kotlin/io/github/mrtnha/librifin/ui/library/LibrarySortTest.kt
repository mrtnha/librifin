package io.github.mrtnha.librifin.ui.library

import io.github.mrtnha.librifin.storage.AuthorSortName
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

    @Test
    fun authorBySurnameAsTheFileSaysElseGuessedOneAuthorsBooksByTitleWithoutAuthorLast() {
        val books = listOf(
            book("Walden", authors = listOf("Henry David Thoreau")),
            book("Notes"),
            book("Great Expectations", authors = listOf("Charles Dickens")),
            book("One Hundred Years of Solitude", authors = listOf("Gabriel García Márquez")),
            book("The Left Hand of Darkness", authors = listOf("Ursula K. Le Guin")),
            book("A Tale of Two Cities", authors = listOf("Charles Dickens")),
        )
        // The guess would file him under M.
        val names = mapOf(
            "One Hundred Years of Solitude" to AuthorSortName("Gabriel García Márquez", "García Márquez, Gabriel"),
        )

        val titles = inLocale(Locale.ENGLISH) { sorted(books, LibrarySort.AUTHOR, authorSortNames = names) }

        assertEquals(
            listOf(
                "A Tale of Two Cities",
                "Great Expectations",
                "One Hundred Years of Solitude",
                "The Left Hand of Darkness",
                "Walden",
                "Notes",
            ),
            titles,
        )
    }

    @Test
    fun authorNameReadForAnotherAuthorIsIgnored() {
        val books = listOf(
            book("Walden", authors = listOf("Henry David Thoreau")),
            book("Buddenbrooks", authors = listOf("Thomas Mann")),
        )
        // Read when Jellyfin still had another author for the book.
        val names = mapOf("Walden" to AuthorSortName("Someone Else", "Aaron, Someone"))

        assertEquals(listOf("Buddenbrooks", "Walden"), sorted(books, LibrarySort.AUTHOR, authorSortNames = names))
    }

    @Test
    fun progressFurthestReadFirstThenUnreadByTitle() {
        val books = listOf(book("Walden"), book("Emma"), book("The Prince"), book("Short Fiction"), book("Buddenbrooks"))
        val progress = mapOf("Walden" to reading(0.3f), "The Prince" to finished(), "Short Fiction" to reading(0.8f))

        assertEquals(
            listOf("The Prince", "Short Fiction", "Walden", "Buddenbrooks", "Emma"),
            sorted(books, LibrarySort.PROGRESS, progress = progress),
        )
    }

    @Test
    fun finishedCountsAsFullEvenIfFinishedEarlier() {
        val books = listOf(book("Almost"), book("Done"))
        // Finished at 95 %, like the cover's bar shows it full.
        val progress = mapOf("Almost" to reading(0.99f), "Done" to finished(0.95f))

        assertEquals(listOf("Done", "Almost"), sorted(books, LibrarySort.PROGRESS, progress = progress))
    }

    @Test
    fun equalProgressMostRecentlyReadFirstThenByTitle() {
        val books = listOf(
            book("Walden"),
            book("Emma"),
            book("Buddenbrooks"),
            book("The Prince"),
            book("Anna Karenina"),
            book("Short Fiction"),
            book("Middlemarch"),
        )
        // Buddenbrooks and Anna Karenina: marked played in Jellyfin, with no time it was read.
        val progress = mapOf(
            "Walden" to finished(),
            "Emma" to finished(),
            "Buddenbrooks" to finished(),
            "The Prince" to finished(),
            "Anna Karenina" to finished(),
        )
        // Middlemarch was paged back to its start: read, but at 0 %.
        val lastRead = mapOf("Walden" to 100L, "Emma" to 300L, "The Prince" to 200L, "Middlemarch" to 50L)

        assertEquals(
            listOf(
                "Emma",
                "The Prince",
                "Walden",
                "Anna Karenina",
                "Buddenbrooks",
                "Middlemarch",
                "Short Fiction",
            ),
            sorted(books, LibrarySort.PROGRESS, lastRead = lastRead, progress = progress),
        )
    }

    @Test
    fun surnameIsGuessedAsLibrariesFileNames() {
        mapOf(
            "Terry Pratchett" to "Pratchett, Terry",
            "Martin Luther King Jr." to "King, Martin Luther Jr.",
            "Martin Luther King, Jr." to "King, Martin Luther Jr.",
            "E. M. Forster" to "Forster, E. M.",
            "Homer" to "Homer",
            "Ursula K. Le Guin" to "Le Guin, Ursula K.",
            "Thomas De Quincey" to "De Quincey, Thomas",
            "Johann Wolfgang von Goethe" to "Goethe, Johann Wolfgang von",
            "Ludwig van Beethoven" to "Beethoven, Ludwig van",
            "Honoré de Balzac" to "Balzac, Honoré de",
            "Van Morrison" to "Morrison, Van",
            "Mann, Thomas" to "Mann, Thomas",
        ).forEach { (name, expected) -> assertEquals(expected, surnameFirst(name), name) }
    }

    /** The book's id is its title, so tests can refer to books by title. */
    private fun book(
        title: String,
        sortTitle: String? = null,
        dateAddedMillis: Long? = null,
        authors: List<String> = emptyList(),
    ) = Book(
        id = title,
        title = title,
        coverUrl = null,
        sortTitle = sortTitle,
        dateAddedMillis = dateAddedMillis,
        authors = authors,
    )

    private fun sorted(
        books: List<Book>,
        sort: LibrarySort,
        lastRead: Map<String, Long> = emptyMap(),
        authorSortNames: Map<String, AuthorSortName> = emptyMap(),
        progress: Map<String, BookProgress> = emptyMap(),
    ) = sortBooks(books, sort, progress, lastRead, authorSortNames).map { it.title }

    private fun reading(fraction: Float) = BookProgress(fraction, isFinished = false)

    private fun finished(fraction: Float = 1f) = BookProgress(fraction, isFinished = true)

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
