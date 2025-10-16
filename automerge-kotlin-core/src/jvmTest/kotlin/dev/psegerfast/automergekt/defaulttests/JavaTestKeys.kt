package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.junit.jupiter.api.Assertions
import java.util.Optional

internal class JavaTestKeys : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testKeys() {
        val doc = Document()
        var tx = doc.startTransaction()
        tx[ObjectId.ROOT, "key1"] = ObjectType.MAP
        tx[ObjectId.ROOT, "key2"] = ObjectType.LIST
        val list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)

        val expected = arrayOf("key1", "key2", "list")
        Assertions.assertArrayEquals(expected, tx.keys(ObjectId.ROOT).get())
        Assertions.assertArrayEquals(expected, doc.keys(ObjectId.ROOT).get())

        tx.commit()

        val heads = doc.heads
        tx = doc.startTransaction()
        val expectedBefore = arrayOf("key1", "key2", "list")
        tx.delete(ObjectId.ROOT, "key1")
        val expectedAfter = arrayOf("key2", "list")
        Assertions.assertArrayEquals(expectedAfter, tx.keys(ObjectId.ROOT).get())
        Assertions.assertArrayEquals(expectedBefore, tx.keys(ObjectId.ROOT, heads).get())
        Assertions.assertArrayEquals(expectedBefore, doc.keys(ObjectId.ROOT, heads).get())
        Assertions.assertEquals(Optional.empty<Any>(), tx.keys(list))
        tx.commit()
        Assertions.assertArrayEquals(expectedBefore, doc.keys(ObjectId.ROOT, heads).get())
        Assertions.assertEquals(Optional.empty<Any>(), doc.keys(list))
    }
}
