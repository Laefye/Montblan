package ru.montblan.crypto

import java.security.MessageDigest

class Cryptography {
    companion object {
        fun hash(data: ByteArray): ByteArray {
            return MessageDigest.getInstance("SHA-1").digest(data)
        }
    }
}