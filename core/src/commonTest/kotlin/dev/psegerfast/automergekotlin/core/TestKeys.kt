package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestKeys : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testKeys() {
        val doc = KDocument()
        var tx = doc.startTransaction()
        tx[KObjectId.Companion.ROOT, "key1"] = KObjectType.MAP
        tx[KObjectId.Companion.ROOT, "key2"] = KObjectType.LIST
        val list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)

        val expected = listOf("key1", "key2", "list")
        Assertions.assertIterableEquals(expected, tx.keys(KObjectId.Companion.ROOT))
        Assertions.assertIterableEquals(expected, doc.keys(KObjectId.Companion.ROOT))

        tx.commit()

        val heads = doc.heads
        tx = doc.startTransaction()
        val expectedBefore = listOf("key1", "key2", "list")
        tx.delete(KObjectId.Companion.ROOT, "key1")
        val expectedAfter = listOf("key2", "list")
        Assertions.assertIterableEquals(expectedAfter, tx.keys(KObjectId.Companion.ROOT))
        Assertions.assertIterableEquals(expectedBefore, tx.keys(KObjectId.Companion.ROOT, heads))
        Assertions.assertIterableEquals(expectedBefore, doc.keys(KObjectId.Companion.ROOT, heads))
        Assertions.assertEquals(null, tx.keys(list))
        tx.commit()
        Assertions.assertIterableEquals(expectedBefore, doc.keys(KObjectId.Companion.ROOT, heads))
        Assertions.assertEquals(null, doc.keys(list))
    }
}
