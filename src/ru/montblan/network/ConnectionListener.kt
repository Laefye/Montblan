package ru.montblan.network

interface ConnectionListener {
    fun onPacketReceived(host: String, packet: Packet): Packet?
}