package dev.psegerfast.automergekotlin.core

///**
// * This file is for testing the Kotlin Automerge library. It generates a document with lots of data and nested objects.
// * It then prints the document and its byte representation to the console.
// * */
//fun main(vararg args: String) {
//    val document = KDocument()
//    document.startTransaction().apply {
//        set(KObjectId.ROOT, "text", KObjectType.TEXT)
//    }.commit()
//
//    val documentBytes = document.save()
//    println("KDocument: $document")
//    val byteArrayString = documentBytes.toList().chunked(20).let { chunks ->
//        chunks.map { chunk ->
//            chunk.joinToString(separator = "") { "$it, " }
//        }.joinToString(
//            separator = System.lineSeparator(),
//            prefix = "byteArrayOf(${System.lineSeparator()}",
//            postfix = "${System.lineSeparator()})",
//        ) {
//            "\t$it"
//        }
//    }
//    println(byteArrayString)
//
//    val tree = document.nodeTree()
//    println(tree.nodeString())
//}

sealed interface ReadNode {
    data class ValueNode(val value: KAmValue): ReadNode
    data class TextNode(override val objectId: KObjectId, val value: String): KObjectIdNode
    data class ListNode(override val objectId: KObjectId, val values: List<ReadNode>):
        KObjectIdNode
    data class MapNode(override val objectId: KObjectId, val map: Map<String, ReadNode>):
        KObjectIdNode
    sealed interface KObjectIdNode: ReadNode {
        val objectId: KObjectId
    }
}

fun ReadNode.nodeString(level: Int = 0): String = when(this) {
    is ReadNode.ListNode -> buildString {
        appendLine("[")
        values.map { it.nodeString(level) }.forEachIndexed { index, item ->
            val comma = if(index == values.lastIndex) "" else ","
            appendLine("${getTabs(level + 1)}$item$comma")
        }
        append("${getTabs(level)}]")
    }
    is ReadNode.MapNode -> buildString {
        appendLine("\"$objectId\": {")
        map.entries.forEachIndexed { index, (key, value) ->
            val comma = if(index == map.size - 1) "" else ","
            appendLine("${getTabs(level + 1)}\"$key\": ${value.nodeString(level + 1)}$comma")
        }
        append("${getTabs(level)}}")
    }
    is ReadNode.ValueNode -> value.stringValue()
    is ReadNode.TextNode -> "\"$value\""
}

fun getTabs(level: Int): String = buildString {
    repeat(level) {
        append("\t")
    }
}

fun KReadable.treeString(): String = nodeTree().nodeString()

fun KReadable.nodeTree(): ReadNode.MapNode {
    val rootMapEntries = this.mapEntries(KObjectId.ROOT)!!
    val map = rootMapEntries.associate { (key, value) ->
        key to value.toDocumentNode(this)
    }
    return ReadNode.MapNode(KObjectId.ROOT, map)
}

fun KAmValue.toDocumentNode(readable: KReadable): ReadNode = when (this) {
    is KAmValue.Map -> {
        val objectId = this.id
        val entries = readable.mapEntries(objectId)!!
        val map = entries.associate { (key, value) ->
            key to value.toDocumentNode(readable)
        }
        ReadNode.MapNode(objectId, map)
    }

    is KAmValue.List -> {
        val objectId = this.id
        val items = readable.listItems(objectId)!!
        ReadNode.ListNode(objectId, items.map { it.toDocumentNode(readable) })
    }

    is KAmValue.Text -> {
        val objectId = this.id
        val text = readable.text(objectId)!!
        ReadNode.TextNode(objectId, text)
    }

    else -> ReadNode.ValueNode(this)
}


fun KAmValue.stringValue(): String {
    val value = when(this) {
        is KAmValue.UInt -> "${this.value}"
        is KAmValue.Int -> "${this.value}"
        is KAmValue.Bool -> "${this.value}"
        is KAmValue.Bytes -> "${this.value}"
        is KAmValue.Str -> "\"${this.value}\""
        is KAmValue.F64 -> "${this.value}"
        is KAmValue.Counter -> "${this.value}"
        is KAmValue.Timestamp -> "${this.value}"
        is KAmValue.Null -> null
        is KAmValue.Unknown -> "${this.value.toList()}"
        is KAmValue.Map -> "${this.id}"
        is KAmValue.List -> "${this.id}"
        is KAmValue.Text -> "${this.id}"
        else -> error("Unsupported KAmValue: ${this::class.qualifiedName}")
    }
    return "[${ this::class.simpleName }]: $value"
}