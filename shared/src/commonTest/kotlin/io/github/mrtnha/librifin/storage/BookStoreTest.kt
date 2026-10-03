package io.github.mrtnha.librifin.storage

import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.api.Session
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.writeString

class BookStoreTest {
    private val dir = Path(SystemTemporaryDirectory, "librifin-test-${Random.nextLong().toULong()}")
    private val store = BookStore(dir.toString()).also { SystemFileSystem.createDirectories(dir) }

    private val server = Server(baseUrl = "http://192.168.1.10:8096", id = "server1", name = "home", version = null)
    private val alice = Session(server, userId = "alice", userName = "alice", userImageTag = null, accessToken = "a")
    private val bob = Session(server, userId = "bob", userName = "bob", userImageTag = null, accessToken = "b")
    private val position = ReadingPosition(locator = "{}", progress = 0.4, updatedAtMillis = 1)

    @AfterTest
    fun deleteDir() = deleteRecursively(dir)

    @Test
    fun positionsBelongToTheUserWhoReadThere() = runBlocking {
        store.savePosition(alice, "book1", position)

        assertEquals(0.4, store.readPosition(alice, "book1")?.progress)
        assertEquals(listOf("book1"), store.unsyncedBookIds(alice))

        assertNull(store.readPosition(bob, "book1"))
        assertEquals(emptyMap(), store.readAllPositions(bob))
        assertEquals(emptyList(), store.unsyncedBookIds(bob))
    }

    @Test
    fun sameUserIdOnAnotherServerIsSomeoneElse() = runBlocking {
        val otherServer = alice.copy(server = server.copy(id = "server2"))
        store.savePosition(alice, "book1", position)

        assertNull(store.readPosition(otherServer, "book1"))
    }

    @Test
    fun newPositionIsUnsyncedButKeepsWhatTheServerHad() = runBlocking {
        store.savePosition(alice, "book1", position)
        store.markSynced(alice, "book1", store.readPosition(alice, "book1")!!, serverTicks = 4_000_000)

        store.savePosition(alice, "book1", position.copy(progress = 0.5, isSynced = true, serverTicks = 1))

        val saved = store.readPosition(alice, "book1")!!
        assertEquals(0.5, saved.progress)
        assertFalse(saved.isSynced)
        assertEquals(4_000_000, saved.serverTicks)
    }

    @Test
    fun syncedPositionIsNoLongerUnsynced() = runBlocking {
        store.savePosition(alice, "book1", position)
        store.markSynced(alice, "book1", store.readPosition(alice, "book1")!!, serverTicks = 4_000_000)

        val saved = store.readPosition(alice, "book1")!!
        assertTrue(saved.isSynced)
        assertEquals(4_000_000, saved.serverTicks)
        assertEquals(emptyList(), store.unsyncedBookIds(alice))
    }

    @Test
    fun syncOfAnOlderPositionIsSkipped() = runBlocking {
        store.savePosition(alice, "book1", position)
        val sent = store.readPosition(alice, "book1")!!
        store.savePosition(alice, "book1", position.copy(progress = 0.6, updatedAtMillis = 2))

        store.markSynced(alice, "book1", sent, serverTicks = 4_000_000)

        val saved = store.readPosition(alice, "book1")!!
        assertEquals(0.6, saved.progress)
        assertFalse(saved.isSynced)
        assertNull(saved.serverTicks)
        assertEquals(listOf("book1"), store.unsyncedBookIds(alice))
    }

    @Test
    fun markFinishedMarksThePositionFinishedAndUnsynced() = runBlocking {
        store.savePosition(alice, "book1", position)
        store.markSynced(alice, "book1", store.readPosition(alice, "book1")!!, serverTicks = 4_000_000)

        store.markFinished(alice, "book1")

        val saved = store.readPosition(alice, "book1")!!
        assertTrue(saved.isFinished)
        assertFalse(saved.isSynced)
    }

    @Test
    fun markFinishedWithoutPositionDoesNothing() = runBlocking {
        store.markFinished(alice, "book1")

        assertNull(store.readPosition(alice, "book1"))
    }

    @Test
    fun rapidSavesKeepTheirOrder() = runBlocking {
        coroutineScope {
            (1..20).forEach { i ->
                launch(start = CoroutineStart.UNDISPATCHED) {
                    store.savePosition(alice, "book1", position.copy(progress = i / 20.0, updatedAtMillis = i.toLong()))
                }
            }
        }

        assertEquals(1.0, store.readPosition(alice, "book1")?.progress)
    }

    @Test
    fun savedLibraryBelongsToTheUserAndServer() {
        val books = listOf(Book(id = "book1", title = "The Hobbit", coverUrl = null, authors = listOf("J. R. R. Tolkien")))
        store.saveLibrary(alice, books)

        assertEquals(books, store.readLibrary(alice))
        assertNull(store.readLibrary(bob))
        assertNull(store.readLibrary(alice.copy(server = server.copy(id = "server2"))))
    }

    @Test
    fun unreadableFilesCountAsMissing() = runBlocking {
        store.savePosition(alice, "book1", position)
        writeText(Path(dir, "positions", "server1", "alice", "book1.json"), "{ not json")
        writeText(Path(dir, "library.json"), "")

        assertNull(store.readPosition(alice, "book1"))
        assertNull(store.readLibrary(alice))
    }

    private fun writeText(file: Path, text: String) =
        SystemFileSystem.sink(file).buffered().use { it.writeString(text) }

    private fun deleteRecursively(path: Path) {
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
