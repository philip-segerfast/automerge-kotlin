package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestGetAt : AnnotationSpec() {
    internal interface TestCase {
        fun init(doc: KTransaction, value: String)

        fun update(tx: KTransaction, value: String)

        fun get(r: KReadable): KAmValue

        fun getAt(r: KReadable, heads: Array<KChangeHash>): KAmValue
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    private fun run(testcase: TestCase) {
        // Steps
        // Create the document
        val doc = KDocument()
        // Create the first state and commit
        var tx = doc.startTransaction()
        testcase.init(tx, "value1")
        tx.commit()
        // save the heads
        val heads = doc.heads
        // Create a new transaction
        tx = doc.startTransaction()
        // Run the second update
        testcase.update(tx, "value2")
        tx.commit()
        // Check that get returns current value
        tx = doc.startTransaction()
        // Check that get and getAt on open tx work as expected
        Assertions.assertEquals("value2", (testcase.get(tx) as KAmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(tx, heads) as KAmValue.Str).value)

        // Check that get and getAt on document with open tx work
        Assertions.assertEquals("value2", (testcase.get(doc) as KAmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(doc, heads) as KAmValue.Str).value)

        tx.commit()

        // Check that get and getAt on doc with closed tx work as expected
        Assertions.assertEquals("value2", (testcase.get(doc) as KAmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(doc, heads) as KAmValue.Str).value)
    }

    @Test
    fun testGetAtInMap() {
        run(object : TestCase {
            override fun init(doc: KTransaction, value: String) {
                doc[KObjectId.Companion.ROOT, "key"] = value
            }

            override fun update(tx: KTransaction, value: String) {
                tx[KObjectId.Companion.ROOT, "key"] = value
            }

            override fun get(r: KReadable): KAmValue {
                return r[KObjectId.Companion.ROOT, "key"]!!
            }

            override fun getAt(r: KReadable, heads: Array<KChangeHash>): KAmValue {
                return r[KObjectId.Companion.ROOT, "key", heads]!!
            }
        })
    }

    @Test
    fun testGetAtInList() {
        run(object : TestCase {
            lateinit var list: KObjectId

            override fun init(doc: KTransaction, value: String) {
                list = doc.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
                doc.insert(list, 0, value)
            }

            override fun update(tx: KTransaction, value: String) {
                tx[list, 0] = value
            }

            override fun get(r: KReadable): KAmValue {
                return r[list, 0]!!
            }

            override fun getAt(r: KReadable, heads: Array<KChangeHash>): KAmValue {
                return r[list, 0, heads]!!
            }
        })
    }
}
