package org.mersenne.primenet.compress

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class Bzip2 private constructor() {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun extract(archive: ByteArray): InputStream = BZip2CompressorInputStream(ByteArrayInputStream(archive))
    }
}
