package dev.psegerfast.automergekt.defaulttests

import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class TestText {
    private lateinit var doc: Document
    private lateinit var text: ObjectId

    @BeforeEach
    fun setup() {
        doc = Document()
        val tx = doc.startTransaction()
        text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
        tx.commit()
    }

    @Test
    fun testGet() {
        val tx = doc.startTransaction()
        tx.spliceText(text, 0, 0, "hello")
        Assertions.assertEquals(Optional.of("hello"), tx.text(text))
        Assertions.assertEquals(Optional.of("hello"), doc.text(text))
    }

    @Test
    fun testGetNonTextInDoc() {
        val otherDoc = Document()
        val otherTx = otherDoc.startTransaction()
        val map = otherTx.set(ObjectId.ROOT, "map", ObjectType.MAP)
        otherTx.commit()
        Assertions.assertEquals(otherDoc.text(map), Optional.empty<Any>())
    }

    @Test
    fun testGetNonTextInTx() {
        val otherDoc = Document()
        val otherTx = otherDoc.startTransaction()
        val map = otherTx.set(ObjectId.ROOT, "map", ObjectType.MAP)
        Assertions.assertEquals(otherTx.text(map), Optional.empty<Any>())
    }

    @Test
    fun testTextAt() {
        var tx = doc.startTransaction()
        tx.spliceText(text, 0, 0, "hello")
        tx.commit()
        val heads = doc.heads
        tx = doc.startTransaction()
        tx.spliceText(text, 5, 0, " world")
        Assertions.assertEquals(Optional.of("hello world"), tx.text(text))
        Assertions.assertEquals(Optional.of("hello"), tx.text(text, heads))
        Assertions.assertEquals(Optional.of("hello"), doc.text(text, heads))
        tx.commit()
        Assertions.assertEquals(Optional.of("hello"), doc.text(text, heads))
    }
}
