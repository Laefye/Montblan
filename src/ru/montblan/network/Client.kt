package ru.montblan.network

import ru.montblan.crypto.Cryptography
import ru.montblan.crypto.SignKey
import ru.montblan.crypto.VerifyKey
import ru.montblan.exceptions.NotPingedException

class Client(private val port: Int, private val signKey: SignKey) : ConnectionListener {
    private val nodes = mutableMapOf<Identifier, Node>()
    val connection = Connection(port)
    val id = Identifier(Cryptography.hash(VerifyKey.codec.encode(signKey.createVerifyKey()).bytes))

    init {
        connection.addListener(this)
    }

    private fun pong(): Packet {
        val response = Packet()
        response.writeString("pong")
        return response
    }

    private fun store(host: String, packet: Packet): Packet {
        val port = packet.readInt()
        val verifyKey = packet.readPacket()
        val node = Node(host, port)
        val id = Identifier(Cryptography.hash(verifyKey.bytes))
        if (id != this.id) {
            nodes[id] = node
        }
        return Packet()
    }

    private fun findNode(packet: Packet): Packet {
        val id = Identifier.codec.decode(packet.readPacket())
        val nearest = nodes.keys
            .filter { it != id }
            .sortedBy { it.toDistance(id) }
        val response = Packet()
        response.writeInt(nearest.size)
        for (nearId in nearest) {
            val node = nodes[nearId]!!
            response.writePacket(Identifier.codec.encode(nearId))
            response.writePacket(Node.codec.encode(node))
        }
        return response
    }

    private fun findValue(packet: Packet): Packet {
        val key = Identifier.codec.decode(packet.readPacket())
        val value = nodes[key]
        if (value != null) {
            val response = Packet()
            response.writeString("value")
            response.writePacket(Node.codec.encode(value))
            return response
        }
        val response = Packet()
        response.writeString("nodes")
        val nearest = nodes.keys
            .filter { it != key }
            .sortedBy { it.toDistance(key) }
        response.writeInt(nearest.size)
        for (nearId in nearest) {
            val node = nodes[nearId]!!
            response.writePacket(Identifier.codec.encode(nearId))
            response.writePacket(Node.codec.encode(node))
        }
        return response
    }

    override fun onPacketReceived(host: String, packet: Packet): Packet? {
        val command = packet.readString()
        when (command) {
            "ping" -> return pong()
            "store" -> return store(host, packet)
            "find_node" -> return findNode(packet)
            "find_value" -> return findValue(packet)
        }
        return null
    }

    fun ping(node: Node): Boolean {
        val request = Packet()
        request.writeString("ping")
        try {
            val response = connection.send(node, request)
            val command = response.readString()
            if (command == "pong") {
                return true
            }
        }
        catch (_: Exception) {

        }
        return false
    }

    fun store(node: Node) {
        if (!ping(node)) {
            throw NotPingedException();
        }
        val request = Packet()
        request.writeString("store")
        request.writeInt(port)
        request.writePacket(VerifyKey.codec.encode(signKey.createVerifyKey()))
        connection.send(node, request)
    }

    fun findNode(node: Node, identifier: Identifier): Map<Identifier, Node> {
        if (!ping(node)) {
            throw NotPingedException();
        }
        val nodes = mutableMapOf<Identifier, Node>()
        val request = Packet()
        request.writeString("find_node")
        request.writePacket(Identifier.codec.encode(identifier))
        val response = connection.send(node, request)
        val count = response.readInt()
        for (i in 0..<count) {
            val nearId = Identifier.codec.decode(response.readPacket())
            val nearNode = Node.codec.decode(response.readPacket())
            nodes[nearId] = nearNode
        }
        return nodes
    }

    fun findValue(node: Node, key: Identifier): FindResult {
        if (!ping(node)) {
            throw NotPingedException();
        }
        val request = Packet()
        request.writeString("find_value")
        request.writePacket(Identifier.codec.encode(key))
        val response = connection.send(node, request)
        val command = response.readString()
        if (command == "value") {
            return FindResult.Value(Node.codec.decode(response.readPacket()))
        }
        val count = response.readInt()
        val nodes = mutableMapOf<Identifier, Node>()
        for (i in 0..<count) {
            val nearId = Identifier.codec.decode(response.readPacket())
            val nearNode = Node.codec.decode(response.readPacket())
            nodes[nearId] = nearNode
        }
        return FindResult.NearNodes(nodes)
    }

    fun start() {
        connection.start()
    }

    fun connect(id: Identifier, node: Node) {
        var size = nodes.size
        nodes[id] = node
        while (size != nodes.size) {
            size = nodes.size
            for (near in nodes.values.toList()) {
                nodes.putAll(findNode(near, id))
            }
        }
        val nearest = nodes.keys
            .minByOrNull { it.toDistance(id) }
        store(nodes[nearest]!!)
    }

    fun find(id: Identifier): Node? {
        if (nodes.containsKey(id)) {
            return nodes[id]
        }
        val first = nodes.keys
            .minByOrNull { it.toDistance(id) }
        if (first == null) {
            return null
        }
        val usedNodes = mutableMapOf<Identifier, Node>()
        val length = nodes.count()
        usedNodes[first] = nodes[first]!!
        while (length != usedNodes.size) {
            val nearest = usedNodes.keys
                .minByOrNull { it.toDistance(id) }
            val near = usedNodes[nearest]!!
            when (val result = findValue(near, id)) {
                is FindResult.Value -> {
                    return result.value
                }
                is FindResult.NearNodes -> {
                    usedNodes.putAll(result.nodes)
                }
            }
        }
        return null
    }

    fun stop() {
        connection.stop()
    }
}