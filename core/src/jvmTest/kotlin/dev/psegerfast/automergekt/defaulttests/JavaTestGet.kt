package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Transaction
import java.util.Date
import kotlin.jvm.optionals.getOrNull

internal class JavaTestGet : AnnotationSpec() {
    internal interface ValueCheck

    internal interface TestCase : ValueCheck {
        fun set(tx: Transaction, obj: ObjectId?, key: String?)

        fun set(tx: Transaction, obj: ObjectId?, idx: Long)

        fun check(value: AmValue)
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    private fun makeTestCases(): ArrayList<TestCase> {
        val testCases = ArrayList<TestCase>()
        // Uint
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = NewValue.uint(1)
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx.set(obj, idx, NewValue.uint(1))
            }

            override fun check(value: AmValue) {
                (value as AmValue.UInt).value shouldBe 1
            }
        })
        // Int
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = 1
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = 1
            }

            override fun check(value: AmValue) {
                (value as AmValue.Int).value shouldBe 1
            }
        })
        // F64
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = 2.0
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = 2.0
            }

            override fun check(value: AmValue) {
                (value as AmValue.F64).value shouldBe 2.0
            }
        })
        // Bool
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = true
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = true
            }

            override fun check(value: AmValue) {
                (value as AmValue.Bool).value shouldBe true
            }
        })
        // Str
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = "hello"
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = "hello"
            }

            override fun check(value: AmValue) {
                (value as AmValue.Str).value shouldBe "hello"
            }
        })
        // Bytes
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = byteArrayOf(1, 2, 3)
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = byteArrayOf(1, 2, 3)
            }

            override fun check(value: AmValue) {
                (value as AmValue.Bytes).value shouldBe byteArrayOf(1, 2, 3)
            }
        })
        // Counter TODO - Has been removed due to Counter being package-private
//        testCases.add(object : TestCase {
//            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
//                tx[obj, key] = Counter(1)
//            }
//
//            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
//                tx[obj, idx] = Counter(1)
//            }
//
//            override fun check(value: AmValue) {
//                Assertions.assertEquals(1, (value as AmValue.Counter).value)
//            }
//        })
        // Timestamp
        testCases.add(object : TestCase {
            private val date = Date()

            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = date
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = date
            }

            override fun check(value: AmValue) {
                (value as AmValue.Timestamp).value shouldBe date
            }
        })
        // Null
        testCases.add(object : TestCase {
            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                tx[obj, key] = NewValue.NULL
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                tx[obj, idx] = NewValue.NULL
            }

            override fun check(value: AmValue) {
                value.shouldBeInstanceOf<AmValue.Null>()
            }
        })
        // List
        testCases.add(object : TestCase {
            var list: ObjectId? = null

            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                list = tx.set(obj, key, ObjectType.LIST)
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                list = tx.set(obj, idx, ObjectType.LIST)
            }

            override fun check(value: AmValue) {
                (value as AmValue.List).id shouldBe list
            }
        })
        // Map
        testCases.add(object : TestCase {
            var map: ObjectId? = null

            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                map = tx.set(obj, key, ObjectType.MAP)
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                map = tx.set(obj, idx, ObjectType.MAP)
            }

            override fun check(value: AmValue) {
                (value as AmValue.Map).id shouldBe map
            }
        })
        // Text
        testCases.add(object : TestCase {
            var text: ObjectId? = null

            override fun set(tx: Transaction, obj: ObjectId?, key: String?) {
                text = tx.set(obj, key, ObjectType.TEXT)
            }

            override fun set(tx: Transaction, obj: ObjectId?, idx: Long) {
                text = tx.set(obj, idx, ObjectType.TEXT)
            }

            override fun check(value: AmValue) {
                (value as AmValue.Text).id shouldBe text
            }
        })
        return testCases
    }

    @Test
    fun testGetMap() {
        for (testCase in makeTestCases()) {
            val doc = Document()
            doc.startTransaction().use { tx ->
                testCase.set(tx, ObjectId.ROOT, "key")
                testCase.check(tx[ObjectId.ROOT, "key"].get())
                testCase.check(doc[ObjectId.ROOT, "key"].get())
                tx.commit()
                testCase.check(doc[ObjectId.ROOT, "key"].get())
            }
        }
    }

    @Test
    fun testGetList2() {
        for (testCase in makeTestCases()) {
            val doc = Document()
            doc.startTransaction().use { tx ->
                tx.set(ObjectId.ROOT, "list", ObjectType.LIST).let { listId ->

                    tx.insert(listId, 0, NewValue.NULL)

                    testCase.let { tc ->
                        tc.set(tx, listId, 0)
                        tc.check(tx[listId, 0].get())
//                        tc.check(doc[listId, 0].get())

                        val hash = tx.commit().getOrNull()

                        println("CHANGE HASH: ${ hash?.bytes?.contentToString() }")

                        tc.check(doc[listId, 0].get())
                    }
                }
            }
        }
    }

    @Test
    @Ignore
    fun testGetList() {
        for ((index, testCase) in makeTestCases().withIndex()) {
            println("Index: $index")
            val doc = Document()
            doc.startTransaction().also { tx ->
                val list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
                tx.insert(list, 0, NewValue.NULL) // tx[0] = NULL
                testCase.set(tx, list, 0) // tx[0] = 1
                testCase.check(tx.get(list, 0).get())
                tx.commit()
                val value = doc[list, 0]
                testCase.check(value.get())
            }
        }
    }
}
