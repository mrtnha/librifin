package io.github.mrtnha.librifin.epub

import io.github.mrtnha.librifin.api.FilePart
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringReader
import java.util.zip.DataFormatException
import java.util.zip.Inflater
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException

/**
 * Reads the part of a file that [range] names, in the form of an HTTP Range header: "bytes=-16384" for
 * the last 16 KB, "bytes=100-199" for bytes 100 to 199.
 */
typealias ReadRange = suspend (range: String) -> FilePart

/**
 * How an EPUB files its first author, e.g. "Mann, Thomas" for Thomas Mann (the package document's
 * file-as), or null if it doesn't say or the file isn't an EPUB.
 *
 * Reads only the end of the file, where the ZIP lists its entries, and the package document: usually two
 * requests of a few KB, instead of the whole book. Errors of [read], e.g. no network, are thrown; a file
 * that can't be made sense of is null.
 */
suspend fun readAuthorFileAs(read: ReadRange): String? {
    val zip = ZipDirectory.read(read) ?: return null
    val packagePath = zip.packageDocumentPath() ?: return null
    return zip.entry(packagePath)?.let(::authorFileAs)
}

/**
 * The entries of a ZIP file, from its central directory at the end of the file. Keeps the bytes read
 * from there ([end]), so an entry that lies in them needs no request of its own.
 */
private class ZipDirectory(
    private val read: ReadRange,
    private val fileSize: Long,
    private val end: ByteArray,
    private val entries: Map<String, ZipEntry>,
) {
    private val endStart = fileSize - end.size

    /** The package document's path: the only .opf file, or else the one META-INF/container.xml names. */
    suspend fun packageDocumentPath(): String? {
        val packages = entries.keys.filter { it.endsWith(".opf", ignoreCase = true) }
        if (packages.size == 1) return packages.single()
        return entry(CONTAINER_PATH)?.let(::rootFilePath)
    }

    /** The unpacked content of the entry at [path], or null if there's none or it can't be unpacked. */
    suspend fun entry(path: String): ByteArray? {
        val entry = entries[path] ?: return null
        if (entry.isEncrypted || entry.compressedSize > MAX_ENTRY_SIZE || entry.size > MAX_ENTRY_SIZE) return null
        val compressedSize = entry.compressedSize.toInt()
        // The local header repeats the name and may have an extra field of its own: this usually covers it.
        val expected = LOCAL_HEADER_SIZE + entry.nameLength + entry.extraLength + compressedSize + LOCAL_EXTRA_SLACK
        var bytes = bytesAt(entry.headerOffset, expected)
        if (bytes.size < LOCAL_HEADER_SIZE || bytes.uint(0) != LOCAL_HEADER_SIGNATURE) return null
        val dataStart = LOCAL_HEADER_SIZE + bytes.short(26) + bytes.short(28)
        val dataEnd = dataStart + compressedSize
        if (bytes.size < dataEnd) bytes = bytesAt(entry.headerOffset, dataEnd)
        if (bytes.size < dataEnd) return null
        val data = bytes.copyOfRange(dataStart, dataEnd)
        return when (entry.method) {
            STORED -> data
            DEFLATED -> inflate(data, entry.size.toInt())
            else -> null
        }
    }

    /** [count] bytes from [offset] on, fewer at the end of the file; from [end] if they lie in it. */
    private suspend fun bytesAt(offset: Long, count: Int): ByteArray {
        if (offset !in 0 until fileSize) return ByteArray(0)
        val length = minOf(count.toLong(), fileSize - offset).toInt()
        if (offset >= endStart) {
            val start = (offset - endStart).toInt()
            return end.copyOfRange(start, start + length)
        }
        return read("bytes=$offset-${offset + length - 1}").bytes
    }

    companion object {
        /** Reads the end of the file and the central directory, or null if it isn't a ZIP file. */
        suspend fun read(read: ReadRange): ZipDirectory? {
            val part = read("bytes=-$END_READ_SIZE")
            val end = part.bytes
            val endStart = part.fileSize - end.size
            val record = findEndRecord(end) ?: return null
            val count = end.short(record + 10)
            val directorySize = end.uint(record + 12)
            val directoryOffset = end.uint(record + 16)
            // All ones: the numbers are in a ZIP64 record instead, which EPUBs don't need.
            if (count == 0xFFFF || directoryOffset == 0xFFFFFFFFL) return null
            if (directorySize > MAX_DIRECTORY_SIZE || directoryOffset + directorySize > endStart + record) return null
            val directory = if (directoryOffset >= endStart) {
                val start = (directoryOffset - endStart).toInt()
                end.copyOfRange(start, start + directorySize.toInt())
            } else {
                // Starts before the end that was read: read all of it.
                read("bytes=$directoryOffset-${directoryOffset + directorySize - 1}").bytes
                    .takeIf { it.size.toLong() == directorySize } ?: return null
            }
            val entries = entries(directory, count) ?: return null
            return ZipDirectory(read, part.fileSize, end, entries)
        }

        /** Where the end of central directory record starts: searched backwards, as a comment may follow it. */
        private fun findEndRecord(end: ByteArray): Int? {
            val last = end.size - END_RECORD_SIZE
            val first = maxOf(0, last - MAX_COMMENT_SIZE)
            return (last downTo first).firstOrNull { at ->
                end.uint(at) == END_RECORD_SIGNATURE && at + END_RECORD_SIZE + end.short(at + 20) == end.size
            }
        }

        private fun entries(directory: ByteArray, count: Int): Map<String, ZipEntry>? {
            val entries = HashMap<String, ZipEntry>(count)
            var at = 0
            repeat(count) {
                if (at + CENTRAL_HEADER_SIZE > directory.size) return null
                if (directory.uint(at) != CENTRAL_HEADER_SIGNATURE) return null
                val nameLength = directory.short(at + 28)
                val extraLength = directory.short(at + 30)
                val commentLength = directory.short(at + 32)
                val nameStart = at + CENTRAL_HEADER_SIZE
                if (nameStart + nameLength > directory.size) return null
                entries[directory.decodeToString(nameStart, nameStart + nameLength)] = ZipEntry(
                    flags = directory.short(at + 8),
                    method = directory.short(at + 10),
                    compressedSize = directory.uint(at + 20),
                    size = directory.uint(at + 24),
                    nameLength = nameLength,
                    extraLength = extraLength,
                    headerOffset = directory.uint(at + 42),
                )
                at = nameStart + nameLength + extraLength + commentLength
            }
            return entries
        }
    }
}

/** One file in a ZIP, as its central directory describes it. [size] is its unpacked size. */
private class ZipEntry(
    val flags: Int,
    val method: Int,
    val compressedSize: Long,
    val size: Long,
    val nameLength: Int,
    val extraLength: Int,
    val headerOffset: Long,
) {
    val isEncrypted get() = (flags and ENCRYPTED_FLAG) != 0
}

/** [data] unpacked to exactly [size] bytes, or null if it isn't valid. */
private fun inflate(data: ByteArray, size: Int): ByteArray? {
    // Raw deflate, as in ZIP files. It wants one extra byte after the data.
    val inflater = Inflater(true)
    return try {
        inflater.setInput(data.copyOf(data.size + 1))
        val out = ByteArray(size)
        var done = 0
        while (done < size && !inflater.finished()) {
            val count = inflater.inflate(out, done, size - done)
            if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
            done += count
        }
        out.takeIf { done == size }
    } catch (_: DataFormatException) {
        null
    } finally {
        inflater.end()
    }
}

/** The package document's path from META-INF/container.xml. */
private fun rootFilePath(container: ByteArray): String? =
    parseXml(container)
        ?.getElementsByTagNameNS(CONTAINER_NS, "rootfile")?.elements()
        ?.firstOrNull { it.attribute("media-type") == PACKAGE_MEDIA_TYPE }
        ?.attribute("full-path")

/**
 * The file-as of the first creator who is an author: one with no role, or with "aut" among their roles
 * (EPUB 3 allows several, e.g. annotator, author and writer of the foreword). EPUB 2 gives the role and
 * file-as as attributes of the creator (opf:role, opf:file-as), EPUB 3 as meta elements that refine the
 * creator by its id.
 */
private fun authorFileAs(packageDocument: ByteArray): String? {
    val document = parseXml(packageDocument) ?: return null
    val metas = document.getElementsByTagNameNS(OPF_NS, "meta").elements()
    fun refinements(creator: Element, property: String): List<String> {
        val id = creator.attribute("id") ?: return emptyList()
        return metas
            .filter { it.attribute("refines") == "#$id" && it.attribute("property") == property }
            .mapNotNull { it.textContent?.normalized() }
    }
    val author = document.getElementsByTagNameNS(DC_NS, "creator").elements().firstOrNull { creator ->
        val roles = listOfNotNull(creator.attribute("role", OPF_NS)) + refinements(creator, "role")
        roles.isEmpty() || AUTHOR_ROLE in roles
    } ?: return null
    return author.attribute("file-as", OPF_NS) ?: refinements(author, "file-as").firstOrNull()
}

/** [bytes] as an XML document, or null if they aren't one. Nothing is loaded from elsewhere, e.g. a DTD. */
private fun parseXml(bytes: ByteArray): Document? =
    try {
        val builder = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder()
        builder.setEntityResolver { _, _ -> InputSource(StringReader("")) }
        builder.parse(ByteArrayInputStream(bytes))
    } catch (_: SAXException) {
        null
    } catch (_: IOException) {
        null
    } catch (_: ParserConfigurationException) {
        null
    }

private fun NodeList.elements(): List<Element> = (0 until length).mapNotNull { item(it) as? Element }

/** The attribute's text, or null if it's missing or blank. */
private fun Element.attribute(name: String, namespace: String? = null): String? =
    (if (namespace == null) getAttribute(name) else getAttributeNS(namespace, name))?.normalized()

/** Trimmed, with each run of spaces and line breaks as one space; null if nothing is left. */
private fun String.normalized(): String? = trim().replace(Regex("\\s+"), " ").ifEmpty { null }

/** ZIP stores numbers little-endian. */
private fun ByteArray.short(at: Int): Int = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.uint(at: Int): Long = short(at).toLong() or (short(at + 2).toLong() shl 16)

/** Covers the central directory of most EPUBs, so one request usually gets all of it. */
private const val END_READ_SIZE = 16 * 1024

/** Larger than any metadata or directory of a real EPUB; protects against broken or hostile files. */
private const val MAX_ENTRY_SIZE = 1024 * 1024L
private const val MAX_DIRECTORY_SIZE = 4 * 1024 * 1024L

private const val END_RECORD_SIGNATURE = 0x06054b50L
private const val CENTRAL_HEADER_SIGNATURE = 0x02014b50L
private const val LOCAL_HEADER_SIGNATURE = 0x04034b50L
private const val END_RECORD_SIZE = 22
private const val CENTRAL_HEADER_SIZE = 46
private const val LOCAL_HEADER_SIZE = 30
private const val MAX_COMMENT_SIZE = 0xFFFF
private const val LOCAL_EXTRA_SLACK = 256
private const val ENCRYPTED_FLAG = 1
private const val STORED = 0
private const val DEFLATED = 8

private const val CONTAINER_PATH = "META-INF/container.xml"
private const val CONTAINER_NS = "urn:oasis:names:tc:opendocument:xmlns:container"
private const val PACKAGE_MEDIA_TYPE = "application/oebps-package+xml"
private const val OPF_NS = "http://www.idpf.org/2007/opf"
private const val DC_NS = "http://purl.org/dc/elements/1.1/"
private const val AUTHOR_ROLE = "aut"
