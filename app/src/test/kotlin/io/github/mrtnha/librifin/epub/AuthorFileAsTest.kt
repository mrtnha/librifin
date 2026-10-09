package io.github.mrtnha.librifin.epub

import io.github.mrtnha.librifin.api.FilePart
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class AuthorFileAsTest {

    @Test
    fun epub3FileAsRefinesTheAuthorInTwoRequests() = runBlocking {
        val file = RemoteFile(
            epub(
                metadata = """
                    <dc:creator id="author">Thomas Mann</dc:creator>
                    <meta property="file-as" refines="#author">Mann, Thomas</meta>
                """,
                // Chapters after the package document, so it isn't in the end that's read first.
                chapters = 3,
            ),
        )

        assertEquals("Mann, Thomas", readAuthorFileAs(file::read))
        assertTrue(file.requests <= 2, "${file.requests} requests")
    }

    @Test
    fun epub2FileAsIsAnAttribute() = runBlocking {
        val file = RemoteFile(
            epub(metadata = """<dc:creator opf:role="aut" opf:file-as="Dickens, Charles">Charles Dickens</dc:creator>"""),
        )

        assertEquals("Dickens, Charles", readAuthorFileAs(file::read))
    }

    @Test
    fun creatorsWhoArentAuthorsAreSkipped() = runBlocking {
        val file = RemoteFile(
            epub(
                metadata = """
                    <dc:creator id="illustrator">Franz Xaver Winterhalter</dc:creator>
                    <meta property="role" refines="#illustrator" scheme="marc:relators">ill</meta>
                    <meta property="file-as" refines="#illustrator">Winterhalter, Franz Xaver</meta>
                    <dc:creator id="author">Thomas Mann</dc:creator>
                    <meta property="role" refines="#author" scheme="marc:relators">aut</meta>
                    <meta property="file-as" refines="#author">
                        Mann,
                        Thomas
                    </meta>
                """,
            ),
        )

        assertEquals("Mann, Thomas", readAuthorFileAs(file::read))
    }

    @Test
    fun anAuthorCanHaveSeveralRoles() = runBlocking {
        // As in Standard Ebooks' Poe: annotator, author and writer of the foreword, in this order.
        val file = RemoteFile(
            epub(
                metadata = """
                    <dc:creator id="author">Edgar Allan Poe</dc:creator>
                    <meta property="file-as" refines="#author">Poe, Edgar Allan</meta>
                    <meta property="role" refines="#author" scheme="marc:relators">ann</meta>
                    <meta property="role" refines="#author" scheme="marc:relators">aut</meta>
                    <meta property="role" refines="#author" scheme="marc:relators">wfw</meta>
                """,
            ),
        )

        assertEquals("Poe, Edgar Allan", readAuthorFileAs(file::read))
    }

    @Test
    fun withoutFileAsThereIsNone() = runBlocking {
        val file = RemoteFile(epub(metadata = """<dc:creator id="author">Thomas Mann</dc:creator>"""))

        assertNull(readAuthorFileAs(file::read))
    }

    @Test
    fun storedPackageDocument() = runBlocking {
        val file = RemoteFile(
            epub(
                metadata = """<dc:creator opf:file-as="Poe, Edgar Allan">Edgar Allan Poe</dc:creator>""",
                compressed = false,
            ),
        )

        assertEquals("Poe, Edgar Allan", readAuthorFileAs(file::read))
    }

    @Test
    fun withSeveralPackageDocumentsTheContainerSaysWhich() = runBlocking {
        val file = RemoteFile(
            zip(
                "META-INF/container.xml" to container("second.opf").toByteArray(),
                "first.opf" to opf("""<dc:creator opf:file-as="Wrong, Author">Author Wrong</dc:creator>""").toByteArray(),
                "second.opf" to opf("""<dc:creator opf:file-as="Thoreau, Henry David">Henry David Thoreau</dc:creator>""")
                    .toByteArray(),
            ),
        )

        assertEquals("Thoreau, Henry David", readAuthorFileAs(file::read))
    }

    @Test
    fun aLongCentralDirectoryIsReadInFull() = runBlocking {
        val pages = (1..400).map { "OEBPS/text/a-chapter-with-a-rather-long-file-name-$it.xhtml" to "<p>$it</p>".toByteArray() }
        val file = RemoteFile(
            zip(
                "META-INF/container.xml" to container("OEBPS/content.opf").toByteArray(),
                "OEBPS/content.opf" to opf("""<dc:creator opf:file-as="Nietzsche, Friedrich">Friedrich Nietzsche</dc:creator>""")
                    .toByteArray(),
                *pages.toTypedArray(),
            ),
        )

        assertEquals("Nietzsche, Friedrich", readAuthorFileAs(file::read))
    }

    @Test
    fun aFileThatIsntAZipHasNone() = runBlocking {
        val file = RemoteFile("%PDF-1.7\n".toByteArray() + Random(1).nextBytes(50_000))

        assertNull(readAuthorFileAs(file::read))
    }

    /** A file on a server that sends parts of it, and counts the requests for them. */
    private class RemoteFile(private val bytes: ByteArray) {
        var requests = 0
            private set

        suspend fun read(range: String): FilePart {
            requests++
            val spec = range.removePrefix("bytes=")
            val (start, end) = if (spec.startsWith("-")) {
                maxOf(0, bytes.size - spec.drop(1).toInt()) to bytes.lastIndex
            } else {
                spec.split("-").let { (first, last) -> first.toInt() to minOf(last.toInt(), bytes.lastIndex) }
            }
            return FilePart(bytes.copyOfRange(start, end + 1), bytes.size.toLong())
        }
    }

    /**
     * An EPUB with a package document with [metadata], and [chapters] chapters of 100 KB that don't
     * compress, after it.
     */
    private fun epub(metadata: String, chapters: Int = 0, compressed: Boolean = true): ByteArray {
        val random = Random(1)
        return zip(
            "mimetype" to "application/epub+zip".toByteArray(),
            "META-INF/container.xml" to container("OEBPS/content.opf").toByteArray(),
            "OEBPS/content.opf" to opf(metadata).toByteArray(),
            *(1..chapters).map { "OEBPS/chapter-$it.xhtml" to random.nextBytes(100_000) }.toTypedArray(),
            compressed = compressed,
        )
    }

    private fun opf(metadata: String) =
        """<package xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/" """ +
            """xmlns:opf="http://www.idpf.org/2007/opf" version="3.0"><metadata>$metadata</metadata></package>"""

    private fun container(packagePath: String) =
        """<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles>""" +
            """<rootfile full-path="$packagePath" media-type="application/oebps-package+xml"/></rootfiles></container>"""

    private fun zip(vararg entries: Pair<String, ByteArray>, compressed: Boolean = true): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, content) in entries) {
                val entry = ZipEntry(name)
                if (!compressed) {
                    entry.method = ZipEntry.STORED
                    entry.size = content.size.toLong()
                    entry.compressedSize = content.size.toLong()
                    entry.crc = CRC32().apply { update(content) }.value
                }
                zip.putNextEntry(entry)
                zip.write(content)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }
}
