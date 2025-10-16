package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.ChangeHash
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Read
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions

internal class JavaTestGetAt : AnnotationSpec() {
    internal interface TestCase {
        fun init(doc: Transaction, value: String?)

        fun update(tx: Transaction, value: String?)

        fun get(r: Read): AmValue

        fun getAt(r: Read, heads: Array<ChangeHash?>?): AmValue
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    fun run(testcase: TestCase) {
        // Steps
        // Create the document
        val doc = Document()
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
        Assertions.assertEquals("value2", (testcase.get(tx) as AmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(tx, heads) as AmValue.Str).value)

        // Check that get and getAt on document with open tx work
        Assertions.assertEquals("value2", (testcase.get(doc) as AmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(doc, heads) as AmValue.Str).value)

        tx.commit()

        // Check that get and getAt on doc with closed tx work as expected
        Assertions.assertEquals("value2", (testcase.get(doc) as AmValue.Str).value)
        Assertions.assertEquals("value1", (testcase.getAt(doc, heads) as AmValue.Str).value)
    }

    @Test
    fun testGetAtInMap() {
        run(object : TestCase {
            override fun init(tx: Transaction, value: String?) {
                tx[ObjectId.ROOT, "key"] = value
            }

            override fun update(tx: Transaction, value: String?) {
                tx[ObjectId.ROOT, "key"] = value
            }

            override fun get(r: Read): AmValue {
                return r[ObjectId.ROOT, "key"].get()
            }

            override fun getAt(r: Read, heads: Array<ChangeHash?>?): AmValue {
                return r[ObjectId.ROOT, "key", heads].get()
            }
        })
    }

    @Test
    fun testGetAtInList() {
        run(object : TestCase {
            var list: ObjectId? = null

            override fun init(tx: Transaction, value: String?) {
                list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
                tx.insert(list, 0, value)
            }

            override fun update(tx: Transaction, value: String?) {
                tx[list, 0] = value
            }

            override fun get(r: Read): AmValue {
                return r[list, 0].get()
            }

            override fun getAt(r: Read, heads: Array<ChangeHash?>?): AmValue {
                return r[list, 0, heads].get()
            }
        })
    }
}
