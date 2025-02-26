package ru.montblan.network

import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class Connection(port: Int) {
    private val serverSocket = ServerSocket(port)
    private val listeners = mutableListOf<ConnectionListener>()

    fun addListener(listener: ConnectionListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ConnectionListener) {
        listeners.remove(listener)
    }

    inner class Stream(
        private val socket: Socket
    ) {
        private fun readHeader(input: InputStream): Header {
            val buffer = ByteArray(Header.PACKET_SIZE)
            input.read(buffer)
            return Header.codec.decode(Packet.fromByteArray(buffer))
        }

        fun run() {
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val header = readHeader(input)
            val buffer = ByteArray(header.size)
            input.read(buffer)
            val packet = Packet.fromByteArray(buffer)
            for (listener in listeners) {
                val answer = listener.onPacketReceived(socket.inetAddress.hostAddress, packet.duplicate())
                if (answer != null) {
                    write(answer.bytes, output)
                    break
                }
            }
            socket.close()
        }
    }

    fun stop() {
        serverSocket.close()
    }

    fun start() {
        while (true) {
            val socket: Socket
            try {
                socket = serverSocket.accept()
            } catch (e: SocketException) {
                if (e.message == "Socket closed") break
                throw e
            }
            val stream = Stream(socket)
            Thread(stream::run).start()
        }
    }

    companion object {
        private fun write(body: ByteArray, output: OutputStream) {
            val header = Header(body.size)
            output.write(
                Header.codec.encode(
                Header(header.size)
            ).bytes)
            output.write(body)
        }

        private fun read(input: InputStream): ByteArray {
            val header = Header.codec.decode(
                Packet.fromByteArray(
                    ByteArray(Header.PACKET_SIZE).also { input.read(it) }
                )
            )
            val body = ByteArray(header.size).also { input.read(it) }
            return body
        }
    }

    fun send(node: Node, packet: Packet): Packet {
        val socket = Socket(node.address, node.port)
        val input = socket.getInputStream()
        val output = socket.getOutputStream()
        write(packet.bytes, output)
        val buffer = read(input)
        socket.close()
        return Packet.fromByteArray(buffer)
    }
}