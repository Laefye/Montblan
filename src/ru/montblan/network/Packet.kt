package ru.montblan.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

class Packet(private val byteBuf: ByteBuf = Unpooled.buffer()) {
    companion object {
        fun fromByteArray(bytes: ByteArray): Packet {
            return Packet(Unpooled.wrappedBuffer(bytes))
        }
    }

    fun writeByte(value: Byte) {
        byteBuf.writeByte(value.toInt())
    }

    fun writeInt(value: Int) {
        byteBuf.writeInt(value)
    }

    fun writeBytes(bytes: ByteArray) {
        byteBuf.writeBytes(bytes)
    }

    fun writeLengthBytes(bytes: ByteArray) {
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
    }

    fun writeString(value: String) {
        byteBuf.writeInt(value.length)
        byteBuf.writeCharSequence(value, Charsets.UTF_8)
    }

    fun writePacket(packet: Packet) {
        val bytes = packet.bytes
        byteBuf.writeInt(bytes.size)
        byteBuf.writeBytes(bytes)
    }

    fun readByte(): Byte {
        return byteBuf.readByte()
    }

    fun readInt(): Int {
        return byteBuf.readInt()
    }

    fun readBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        byteBuf.readBytes(bytes)
        return bytes
    }

    fun readLengthBytes(): ByteArray {
        val size = byteBuf.readInt()
        val bytes = ByteArray(size)
        byteBuf.readBytes(bytes)
        return bytes
    }

    fun readString(): String {
        val size = byteBuf.readInt()
        return byteBuf.readCharSequence(size, Charsets.UTF_8).toString()
    }

    fun readPacket(): Packet {
        val size = byteBuf.readInt()
        val readByteBuf = byteBuf.readBytes(size)
        return Packet(readByteBuf)
    }

    val bytes: ByteArray
        get() {
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.duplicate().getBytes(byteBuf.readerIndex(), bytes)
            return bytes
        }

    fun duplicate(): Packet {
        return Packet(byteBuf.duplicate())
    }
}