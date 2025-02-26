package ru.montblan.crypto

import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.DSAPrivateKey
import java.security.spec.DSAPublicKeySpec


class SignKey(private val privateKey: PrivateKey) {
    fun sign(data: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA1withDSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    companion object {
        fun generate(): SignKey {
            val keyPairGenerator = KeyPairGenerator.getInstance("DSA")
            val keyPair = keyPairGenerator.generateKeyPair()
            return SignKey(keyPair.private)
        }
    }

    fun createVerifyKey(): VerifyKey {
        if (privateKey !is DSAPrivateKey) {
            throw IllegalArgumentException("Only DSA keys are supported")
        }
        val y: BigInteger = privateKey.params.g.modPow(privateKey.x, privateKey.params.p)
        val dsaPublicKeySpec = DSAPublicKeySpec(
            y,
            privateKey.params.p,
            privateKey.params.q,
            privateKey.params.g
        );
        val keyFactory = KeyFactory.getInstance("DSA")
        val publicKey = keyFactory.generatePublic(dsaPublicKeySpec)
        return VerifyKey(publicKey)
    }
}