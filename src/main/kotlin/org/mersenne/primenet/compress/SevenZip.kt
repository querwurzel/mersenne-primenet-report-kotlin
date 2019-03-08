package org.mersenne.primenet.compress

import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import java.io.IOException
import java.util.Objects

class SevenZip private constructor(archive: ByteArray) : Iterable<ByteArray>, Iterator<ByteArray> {

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun extract(archive: ByteArray): Iterable<ByteArray> = SevenZip(archive)
    }

    private val archive = SevenZFile(SeekableInMemoryByteChannel(archive))

    private var currentEntry: ByteArray? = null

    override fun iterator(): Iterator<ByteArray> {
        return this
    }

    override fun hasNext(): Boolean {
        try {
            return Objects.nonNull(currentEntry) || run {
                currentEntry = this.nextEntry()
                return true
            }
        } catch (e: NoSuchElementException) {
            return false
        }
    }

    override fun next(): ByteArray {
        if (Objects.isNull(currentEntry)) {
            return this.nextEntry()
        } else {
            val entry = currentEntry
            currentEntry = null
            return entry!!
        }
    }

    @Throws(NoSuchElementException::class)
    private fun nextEntry(): ByteArray {
        try {
            var entry = archive.nextEntry
            while (Objects.nonNull(entry)) {
                if (!entry.isDirectory) {
                    val content = ByteArray(entry.size.toInt())
                    archive.read(content, 0, content.size)
                    return content
                }
                entry = archive.nextEntry
            }

            throw NoSuchElementException()
        } catch (e: IOException) {
            throw NoSuchElementException(e.message)
        }
    }
}
