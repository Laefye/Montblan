package ru.montblan.network

import java.math.BigInteger
import java.util.Base64
import kotlin.experimental.xor

class Identifier(
    private val bytes: ByteArray
) : Comparable<Identifier> {
    companion object {
        private const val LENGTH = 20

        fun fromBase64(base64: String): Identifier {
            return Identifier(Base64.getUrlDecoder().decode(base64))
        }

        val codec = Codec.Builder<Identifier>()
            .encode { identifier ->
                val packet = Packet()
                packet.writeBytes(identifier.bytes)
                packet
            }
            .decode { packet ->
                Identifier(packet.readBytes(LENGTH))
            }
            .build()
    }

    fun toBase64(): String {
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    fun toDistance(other: Identifier): Identifier {
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) {
            result[i] = bytes[i].xor(other.bytes[i])
        }
        return Identifier(result)
    }

    override fun toString(): String {
        return "base64:${toBase64()}"
    }

    override fun compareTo(other: Identifier): Int {
        for (i in bytes.indices) {
            val diff = bytes[i].toInt() - other.bytes[i].toInt()
            if (diff != 0) {
                return diff
            }
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Identifier) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    fun toBigInteger(): BigInteger {
        return BigInteger(bytes)
    }
}