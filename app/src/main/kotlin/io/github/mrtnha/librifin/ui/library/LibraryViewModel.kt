package io.github.mrtnha.librifin.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.BOOK_PROGRESS_TICKS
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.clientErrorStatus
import io.github.mrtnha.librifin.api.toUserMessage
import io.github.mrtnha.librifin.epub.readAuthorFileAs
import io.github.mrtnha.librifin.platform.SettingsStore
import io.github.mrtnha.librifin.storage.AuthorSortName
import io.github.mrtnha.librifin.storage.Book
import io.github.mrtnha.librifin.storage.BookStore
import io.github.mrtnha.librifin.sync.ProgressSync
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import java.text.Collator
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/** How far a book was read: [fraction] 0..1, [isFinished] once the book was read to the end (or nearly). */
data class BookProgress(val fraction: Float, val isFinished: Boolean)

/**
 * The orders the library can be shown in, each in the direction readers expect. Listed in the sort sheet
 * in this order: by how often they're likely used.
 */
enum class LibrarySort {
    /** Most recently read first. Books never read come after them, by title. */
    RECENTLY_READ,

    /** A to Z by the first author's surname, one author's books by title. Books without an author last. */
    AUTHOR,

    /** A to Z. */
    TITLE,

    /** Most recently added to Jellyfin first. */
    DATE_ADDED,
}

sealed interface LibraryState {
    data object Loading : LibraryState
    data object NoBookLibrary : LibraryState
    /**
     * [books] in the chosen order. [progress]: only for books that were started. [isOffline]: the
     * server can't be reached, so this is the saved list and only the books in [downloadedIds] can be
     * opened. [lastReadMillis]: when each book that was read was last read, to sort them again.
     */
    data class Loaded(
        val books: List<Book>,
        val progress: Map<String, BookProgress>,
        val isOffline: Boolean = false,
        val downloadedIds: Set<String> = emptySet(),
        val lastReadMillis: Map<String, Long> = emptyMap(),
    ) : LibraryState {
        fun canOpen(book: Book) = !isOffline || book.id in downloadedIds
    }
    /** [isSessionExpired]: the server no longer accepts the token, so retrying won't help. */
    data class Error(val message: String, val isSessionExpired: Boolean = false) : LibraryState
}

class LibraryViewModel(
    private val session: Session,
    private val jellyfin: JellyfinClient,
    private val bookStore: BookStore,
    private val progressSync: ProgressSync,
    private val settings: SettingsStore,
) : ViewModel() {
    var state by mutableStateOf<LibraryState>(LibraryState.Loading)
        private set

    /** The order of the books, kept between app starts. */
    var sort by mutableStateOf(
        LibrarySort.entries.find { it.name == settings.read(KEY_SORT) } ?: LibrarySort.RECENTLY_READ,
    )
        private set

    var isSortSheetOpen by mutableStateOf(false)

    /**
     * The search text while searching, else null. Filters the books on the device, so it works offline too.
     * Kept while a book is open, so the results are still there when coming back.
     */
    var searchQuery by mutableStateOf<String?>(null)

    private var loadJob: Job? = null

    /**
     * How each book's file files its first author, by book id. Saved on the device, so each book is
     * read only once. Null until loaded from there.
     */
    private var authorSortNames: Map<String, AuthorSortName>? = null

    private var authorJob: Job? = null

    init {
        load()
    }

    /** Loads again, unless a load is already running. */
    fun refresh() {
        if (loadJob?.isActive != true) load()
    }

    /** Shows the books in [sort]'s order right away, and keeps it for the next app start. */
    fun selectSort(sort: LibrarySort) {
        this.sort = sort
        settings.write(KEY_SORT, sort.name)
        val shown = state as? LibraryState.Loaded ?: return
        state = shown.copy(books = sortBooks(shown.books, sort, shown.lastReadMillis, authorSortNames.orEmpty()))
        readMissingAuthorSortNames()
    }

    /**
     * Loads the books from the server. The saved list is shown meanwhile, and stays when the server
     * can't be reached: then only the downloaded books can be opened.
     */
    fun load(): Job {
        loadJob?.cancel()
        return viewModelScope.launch {
            if (authorSortNames == null) {
                authorSortNames = withContext(Dispatchers.IO) { bookStore.readAuthorSortNames(session) }
            }
            val shown = state as? LibraryState.Loaded
            state = if (shown != null) {
                // E.g. back from a book: its progress and place in the order are on this device already,
                // so they show right away, not only once the server has answered below.
                loaded(shown.books, shown.isOffline, shown.downloadedIds)
            } else {
                withContext(Dispatchers.IO) { bookStore.readLibrary(session) }?.let { loaded(it) }
                    ?: LibraryState.Loading
            }
            val saved = (state as? LibraryState.Loaded)?.books
            state = try {
                val libraries = jellyfin.userViews(session).filter { it.collectionType == "books" }
                if (libraries.isEmpty()) {
                    LibraryState.NoBookLibrary
                } else {
                    // Usually one book library; if there are several, show all their books.
                    val books = libraries.flatMap { jellyfin.books(session, it.id) }.map { item ->
                        Book(
                            id = item.id,
                            title = item.name ?: "Untitled",
                            coverUrl = jellyfin.primaryImageUrl(session.server.baseUrl, item, COVER_MAX_WIDTH),
                            serverProgress = item.userData?.playbackPositionTicks
                                ?.takeIf { it > 0 }
                                ?.let { (it.toDouble() / BOOK_PROGRESS_TICKS).coerceIn(0.0, 1.0) },
                            isPlayed = item.userData?.played == true,
                            authors = item.people.orEmpty().filter { it.type == "Author" }.mapNotNull { it.name },
                            lastPlayedMillis = item.userData?.lastPlayedDate
                                ?.let { Instant.parseOrNull(it) }
                                ?.toEpochMilliseconds(),
                            sortTitle = item.sortName,
                            dateAddedMillis = item.dateCreated?.let { Instant.parseOrNull(it) }?.toEpochMilliseconds(),
                        )
                    }
                    withContext(Dispatchers.IO) { bookStore.saveLibrary(session, books) }
                    // The server is reachable again: send what was read offline.
                    progressSync.sendUnsynced(session)
                    loaded(books)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                when {
                    e.clientErrorStatus == HttpStatusCode.Unauthorized ->
                        LibraryState.Error(e.toUserMessage(), isSessionExpired = true)
                    saved != null -> {
                        val downloaded = withContext(Dispatchers.IO) {
                            saved.filter { bookStore.isDownloaded(it.id) }.mapTo(HashSet()) { it.id }
                        }
                        loaded(saved, isOffline = true, downloadedIds = downloaded)
                    }
                    else -> LibraryState.Error(e.toUserMessage())
                }
            }
            // New books from the server, or ones whose author changed.
            readMissingAuthorSortNames()
        }.also { loadJob = it }
    }

    /**
     * While sorting by author: reads from the books' files how they file their first author, for the books
     * where that isn't known yet or was read for another author. In the background, a few books at a time,
     * on its own, so a new load doesn't stop it. Then saves what was read and sorts the books again. Not
     * while offline, and not twice at once.
     */
    private fun readMissingAuthorSortNames() {
        val shown = state as? LibraryState.Loaded ?: return
        if (sort != LibrarySort.AUTHOR || shown.isOffline || authorJob?.isActive == true) return
        val known = authorSortNames.orEmpty()
        val missing = shown.books.filter { book ->
            val author = book.authors.firstOrNull()
            author != null && known[book.id]?.author != author
        }
        if (missing.isEmpty()) return
        authorJob = viewModelScope.launch {
            // Only changed on the main thread: each read switches to the IO threads and back.
            val read = HashMap<String, AuthorSortName>()
            val parallelReads = Semaphore(PARALLEL_AUTHOR_READS)
            try {
                coroutineScope {
                    for (book in missing) {
                        launch {
                            parallelReads.withPermit {
                                val fileAs = try {
                                    withContext(Dispatchers.IO) {
                                        readAuthorFileAs { range -> jellyfin.bookBytes(session, book.id, range) }
                                    }
                                } catch (e: ResponseException) {
                                    // The server refuses this book, e.g. its file is gone: skip it, read the others.
                                    if (e.response.status == HttpStatusCode.Unauthorized) throw e
                                    return@withPermit
                                }
                                read[book.id] = AuthorSortName(book.authors.first(), fileAs)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // The server can't be reached, or doesn't send parts of files: the rest is read next time.
            } finally {
                if (read.isNotEmpty()) addAuthorSortNames(read)
            }
        }
    }

    /** Saves [read] with the names known so far, and sorts the books again with them. */
    private suspend fun addAuthorSortNames(read: Map<String, AuthorSortName>) {
        val names = authorSortNames.orEmpty() + read
        authorSortNames = names
        // Also when the reading was cancelled, so what was read isn't lost.
        withContext(NonCancellable + Dispatchers.IO) { bookStore.saveAuthorSortNames(session, names) }
        val shown = state as? LibraryState.Loaded ?: return
        if (sort == LibrarySort.AUTHOR) {
            state = shown.copy(books = sortBooks(shown.books, sort, shown.lastReadMillis, names))
        }
    }

    /**
     * [books] as the grid shows them, in the chosen [sort], with this device's progress where there is
     * one (exact, and newest for books read here), else Jellyfin's (e.g. read on another device); the
     * same for whether it's finished.
     *
     * A book was last read at the later of when it was read here and when Jellyfin last saw it read, so
     * a book read here moves up right away, also offline, and one read on another device still does.
     * Only turning a page counts as reading, not just opening a book.
     */
    private suspend fun loaded(
        books: List<Book>,
        isOffline: Boolean = false,
        downloadedIds: Set<String> = emptySet(),
    ): LibraryState.Loaded {
        // After the saves still running: leaving a book right after a page turn may not have finished saving it.
        val positions = bookStore.currentPositions(session)
        val progress = books.mapNotNull { book ->
            val local = positions[book.id]
            val progress = BookProgress(
                fraction = (local?.progress ?: book.serverProgress ?: 0.0).toFloat(),
                isFinished = local?.isFinished ?: book.isPlayed,
            )
            (book.id to progress).takeIf { progress.isFinished || progress.fraction > 0f }
        }.toMap()
        val lastRead = books.mapNotNull { book ->
            listOfNotNull(positions[book.id]?.updatedAtMillis, book.lastPlayedMillis).maxOrNull()?.let { book.id to it }
        }.toMap()
        // The sort and author names are read only now, after the wait above, so changes meanwhile aren't lost.
        val ordered = sortBooks(books, sort, lastRead, authorSortNames.orEmpty())
        return LibraryState.Loaded(ordered, progress, isOffline, downloadedIds, lastRead)
    }

    private companion object {
        /** Half the width of a large phone screen in pixels, so covers stay sharp in a two-column grid. */
        const val COVER_MAX_WIDTH = 600

        const val KEY_SORT = "library_sort"

        /** Books whose author is read at the same time, so a large library doesn't take long. */
        const val PARALLEL_AUTHOR_READS = 4
    }
}

/**
 * [books] in [sort]'s order. [lastReadMillis] is when each book that was read was last read,
 * [authorSortNames] how each book's file files its first author. Books that are equal in that order
 * (never read, added at the same time, by the same author) are sorted by title, so the order never
 * depends on the order the server sent them in.
 *
 * Titles and authors are compared in the order of the phone's language, so "Ödipus" comes with the O's
 * and not after Z. Titles as Jellyfin sorts them ("prince" for "The Prince"); in lists saved before
 * Jellyfin's sort title was loaded, the shown title stands in for it until the server is reached.
 */
fun sortBooks(
    books: List<Book>,
    sort: LibrarySort,
    lastReadMillis: Map<String, Long>,
    authorSortNames: Map<String, AuthorSortName>,
): List<Book> {
    val collator = Collator.getInstance()
    val byTitle = compareBy<Book, String>(collator) { it.sortTitle ?: it.title }
    val order = when (sort) {
        LibrarySort.RECENTLY_READ -> compareBy(nullsLast(reverseOrder())) { book: Book -> lastReadMillis[book.id] }
        LibrarySort.AUTHOR -> {
            val keys = books.associate { it.id to authorSortKey(it, authorSortNames[it.id]) }
            compareBy(nullsLast<String>(collator)) { book: Book -> keys[book.id] }
        }
        LibrarySort.TITLE -> byTitle
        LibrarySort.DATE_ADDED -> compareBy(nullsLast(reverseOrder())) { book: Book -> book.dateAddedMillis }
    }
    return books.sortedWith(order.then(byTitle))
}

/**
 * How [book] is filed by its first author: as its file says ([saved]), if that was read for this author,
 * else with a guessed surname. Null if the book has no author.
 */
private fun authorSortKey(book: Book, saved: AuthorSortName?): String? {
    val author = book.authors.firstOrNull() ?: return null
    return saved?.takeIf { it.author == author }?.fileAs ?: surnameFirst(author)
}

/**
 * [name] with the surname first, as libraries file it, guessed for books whose file doesn't say:
 * "Terry Pratchett" → "Pratchett, Terry".
 * - The surname is the last word. Jr., Sr., II, III and IV after it stay at the end:
 *   "Martin Luther King Jr." → "King, Martin Luther Jr.".
 * - A capitalized particle before it is part of the surname: "Ursula K. Le Guin" → "Le Guin, Ursula K.".
 * - A lowercase one stays with the given names: "Johann Wolfgang von Goethe" → "Goethe, Johann Wolfgang von".
 * - A name that already has its surname first ("Mann, Thomas") and a single name ("Homer") stay as they are.
 *
 * Spanish double surnames ("Gabriel García Márquez") and lowercase particles that English files under the
 * particle ("Daphne du Maurier") can't be told from the name: for these, the file has to say.
 */
fun surnameFirst(name: String): String {
    val parts = name.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.drop(1).any { !it.isNameSuffix() }) return name.trim()
    val words = parts.joinToString(" ").split(Regex("\\s+")).toMutableList()
    val suffixes = ArrayDeque<String>()
    while (words.size > 1 && words.last().isNameSuffix()) suffixes.addFirst(words.removeAt(words.lastIndex))
    if (words.size < 2) return name.trim()
    var surnameStart = words.lastIndex
    // Keeps at least one given name: in "Van Morrison", Van is his first name.
    while (surnameStart > 1 && words[surnameStart - 1] in SURNAME_PARTICLES) surnameStart--
    val surname = words.subList(surnameStart, words.size).joinToString(" ")
    val givenNames = words.subList(0, surnameStart).joinToString(" ")
    return (listOf("$surname, $givenNames") + suffixes).joinToString(" ")
}

private fun String.isNameSuffix() = trimEnd('.').lowercase() in NAME_SUFFIXES

private val NAME_SUFFIXES = setOf("jr", "sr", "ii", "iii", "iv")

/** Capitalized, they belong to the surname that follows; lowercase ("von", "de"), to the given names. */
private val SURNAME_PARTICLES = setOf(
    "Le", "La", "De", "Du", "Da", "Di", "Del", "Della", "Des", "Van", "Von", "Der", "Den", "Ten", "Ter",
)
