package ru.montblan.network

import ru.montblan.crypto.VerifyKey

data class Node(
    val address: String,
    val port: Int,
) {
    companion object {
        val codec = Codec.Builder<Node>()
            .encode {
                val packet = Packet()
                packet.writeString(it.address)
                packet.writeInt(it.port)
                packet
            }
            .decode {
                val address = it.readString()
                val port = it.readInt()
                Node(address, port)
            }
            .build()
    }
}
