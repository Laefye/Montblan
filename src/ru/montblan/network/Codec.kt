package ru.montblan.network

abstract class Codec<T> {
    abstract fun encode(value: T): Packet
    abstract fun decode(packet: Packet): T

    class Builder<T> {
        private var encodeFunction: ((T) -> Packet)? = null
        private var decodeFunction: ((Packet) -> T)? = null

        fun encode(encodeFunction: (T) -> Packet): Builder<T> {
            this.encodeFunction = encodeFunction
            return this
        }

        fun decode(decodeFunction: (Packet) -> T): Builder<T> {
            this.decodeFunction = decodeFunction
            return this
        }

        fun build(): Codec<T> {
            if (encodeFunction == null || decodeFunction == null) {
                throw IllegalStateException("encode or decode function is not set")
            }
            return object : Codec<T>() {
                override fun encode(value: T): Packet {
                    return encodeFunction!!(value)
                }
                override fun decode(packet: Packet): T {
                    return decodeFunction!!(packet)
                }
            };
        }
    }
}