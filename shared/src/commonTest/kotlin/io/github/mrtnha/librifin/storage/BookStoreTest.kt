package io.github.mrtnha.librifin.storage

import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.api.Session
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory

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

    private fun deleteRecursively(path: Path) {
        if (SystemFileSystem.metadataOrNull(path)?.isDirectory == true) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
