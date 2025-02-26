package ru.montblan.crypto

import ru.montblan.network.Codec
import ru.montblan.network.Packet
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.DSAPublicKey
import java.security.spec.DSAPublicKeySpec

class VerifyKey(private val publicKey: PublicKey) {
    fun verify(data: ByteArray, sign: ByteArray): Boolean {
        val signature = Signature.getInstance("SHA1withDSA")
        signature.initVerify(publicKey)
        signature.update(data)
        return signature.verify(sign)
    }

    companion object {
        val codec = Codec.Builder<VerifyKey>()
            .encode { verifyKey ->
                val publicKey = verifyKey.publicKey
                if (publicKey !is DSAPublicKey) {
                    throw IllegalArgumentException("public key is not DSA")
                }
                val packet = Packet()
                packet.writeLengthBytes(publicKey.y.toByteArray())
                packet.writeLengthBytes(publicKey.params.p.toByteArray())
                packet.writeLengthBytes(publicKey.params.q.toByteArray())
                packet.writeLengthBytes(publicKey.params.g.toByteArray())
                packet
            }
            .decode { packet ->
                val y = BigInteger(packet.readLengthBytes())
                val p = BigInteger(packet.readLengthBytes())
                val q = BigInteger(packet.readLengthBytes())
                val g = BigInteger(packet.readLengthBytes())
                val dsaPublicKeySpec = DSAPublicKeySpec(y, p, q, g)
                val keyFactory = KeyFactory.getInstance("DSA")
                val publicKey = keyFactory.generatePublic(dsaPublicKeySpec)
                VerifyKey(publicKey)
            }
            .build()
    }
}