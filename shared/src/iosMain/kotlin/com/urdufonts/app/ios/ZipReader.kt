package com.urdufonts.app.ios

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

/**
 * A small ZIP reader for the font archives the API serves (iOS has no java.util.zip). Reads the central
 * directory, so sizes are known even when the local headers use data descriptors, and supports the two
 * methods font zips use: stored (0) and deflate (8, through the system zlib).
 */
internal object ZipReader {

    class Entry(val name: String, val isDirectory: Boolean, private val archive: ByteArray, private val method: Int,
                private val compressedSize: Int, private val size: Int, private val localHeaderOffset: Int) {
        fun read(): ByteArray {
            val nameLen = archive.u16(localHeaderOffset + 26)
            val extraLen = archive.u16(localHeaderOffset + 28)
            val start = localHeaderOffset + 30 + nameLen + extraLen
            val data = archive.copyOfRange(start, start + compressedSize)
            return when (method) {
                0 -> data
                8 -> inflateRaw(data, size)
                else -> throw IllegalStateException("Unsupported zip method $method in $name")
            }
        }
    }

    fun entries(archive: ByteArray): List<Entry> {
        val end = findEndOfCentralDirectory(archive) ?: throw IllegalStateException("Not a zip file")
        val count = archive.u16(end + 10)
        var p = archive.u32(end + 16)
        val result = ArrayList<Entry>(count)
        repeat(count) {
            check(archive.u32(p) == 0x02014b50) { "Broken zip central directory" }
            val method = archive.u16(p + 10)
            val compressed = archive.u32(p + 20)
            val size = archive.u32(p + 24)
            val nameLen = archive.u16(p + 28)
            val extraLen = archive.u16(p + 30)
            val commentLen = archive.u16(p + 32)
            val localOffset = archive.u32(p + 42)
            val name = archive.decodeToString(p + 46, p + 46 + nameLen)
            result += Entry(name, name.endsWith("/"), archive, method, compressed, size, localOffset)
            p += 46 + nameLen + extraLen + commentLen
        }
        return result
    }

    private fun findEndOfCentralDirectory(archive: ByteArray): Int? {
        val min = maxOf(0, archive.size - 22 - 0xFFFF)
        for (i in archive.size - 22 downTo min) if (archive.u32(i) == 0x06054b50) return i
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun inflateRaw(input: ByteArray, size: Int): ByteArray {
        val output = ByteArray(size)
        if (size == 0) return output
        memScoped {
            val stream = alloc<z_stream>()
            // -15: raw deflate data, as stored in zip entries (no zlib header)
            check(inflateInit2(stream.ptr, -15) == Z_OK) { "zlib init failed" }
            try {
                input.usePinned { inPin ->
                    output.usePinned { outPin ->
                        stream.next_in = inPin.addressOf(0).reinterpret()
                        stream.avail_in = input.size.convert()
                        stream.next_out = outPin.addressOf(0).reinterpret()
                        stream.avail_out = size.convert()
                        val status = inflate(stream.ptr, Z_FINISH)
                        check(status == Z_STREAM_END || status == Z_OK) { "zlib inflate failed ($status)" }
                    }
                }
            } finally {
                inflateEnd(stream.ptr)
            }
        }
        return output
    }

    private fun ByteArray.u16(at: Int): Int = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)
    private fun ByteArray.u32(at: Int): Int = u16(at) or (u16(at + 2) shl 16)
}
