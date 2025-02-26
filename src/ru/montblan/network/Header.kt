package ru.montblan.network

data class Header(
    val size: Int,
) {
    companion object {
        const val PACKET_SIZE = 4

        val codec = Codec.Builder<Header>()
            .encode { header ->
                val packet = Packet()
                packet.writeInt(header.size)
                packet
            }
            .decode { packet ->
                val size = packet.readInt()
                Header(size)
            }
            .build()
    }
}
