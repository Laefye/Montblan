package ru.montblan.network

sealed class FindResult {
    data class NearNodes(val nodes: Map<Identifier, Node>) : FindResult()
    data class Value(val value: Node) : FindResult()
}