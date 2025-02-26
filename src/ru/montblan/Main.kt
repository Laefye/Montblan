package ru.montblan

import ru.montblan.crypto.SignKey
import ru.montblan.network.*


fun main(args: Array<String>) {
    val client = Client(args[0].toInt(), SignKey.generate())
    Thread(client::start).start()
    println("> Your id")
    println(client.id.toBase64())
    while (true) {
        val line = readlnOrNull() ?: break
        val parts = line.split(" ")
        when (parts[0]) {
            "stop" -> {
                client.stop()
                break
            }
            "connect" -> {
                client.connect(Identifier.fromBase64(parts[1]), Node(parts[2], parts[3].toInt()))
            }
            "ping" -> {
                val node = client.find(Identifier.fromBase64(parts[1]))
                if (node != null) {
                    println("Status:" + client.ping(node))
                } else {
                    println("Node not found")
                }
            }
        }
    }
}