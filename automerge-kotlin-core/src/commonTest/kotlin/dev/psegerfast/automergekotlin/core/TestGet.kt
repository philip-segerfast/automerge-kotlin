package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.Clock
import kotlin.text.set

internal class TestGet : AnnotationSpec() {
    internal interface ValueCheck

    internal interface TestCase : ValueCheck {
        fun set(tx: KTransaction, obj: KObjectId, key: String)

        fun set(tx: KTransaction, obj: KObjectId, idx: Long)

        fun check(value: KAmValue)
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    private fun makeTestCases(): ArrayList<TestCase> {
        val testCases = ArrayList<TestCase>()
        // Uint
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = KNewValue.Companion.uint(1)
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = KNewValue.Companion.uint(1)
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.UInt).value shouldBe 1
            }
        })
        // Int
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = 1
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = 1
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Int).value shouldBe 1
            }
        })
        // F64
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = 2.0
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = 2.0
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.F64).value shouldBe 2.0
            }
        })
        // Bool
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = true
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = true
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Bool).value shouldBe true
            }
        })
        // Str
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = "hello"
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = "hello"
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Str).value shouldBe "hello"
            }
        })
        // Bytes
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = byteArrayOf(1, 2, 3)
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = byteArrayOf(1, 2, 3)
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Bytes).value shouldBe byteArrayOf(1, 2, 3)
            }
        })
        // Counter TODO - Has been removed due to Counter being package-private
//        testCases.add(object : TestCase {
//            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
//                tx[obj, key] = Counter(1)
//            }
//
//            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
//                tx[obj, idx] = Counter(1)
//            }
//
//            override fun check(value: KAmValue) {
//                Assertions.assertEquals(1, (value as KAmValue.Counter).value)
//            }
//        })
        // Timestamp/Instant
        testCases.add(object : TestCase {
            private val date = Clock.System.now()

            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = date
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = date
            }

            override fun check(value: KAmValue) {
                // Compare seconds because Instant contains very high precision.
                (value as KAmValue.Timestamp).value.epochSeconds shouldBe date.epochSeconds
            }
        })
        // Null
        testCases.add(object : TestCase {
            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                tx[obj, key] = KNewValue.Companion.Null
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                tx[obj, idx] = KNewValue.Companion.Null
            }

            override fun check(value: KAmValue) {
                value.shouldBeInstanceOf<KAmValue.Null>()
            }
        })
        // List
        testCases.add(object : TestCase {
            var list: KObjectId? = null

            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                list = tx.set(obj, key, KObjectType.LIST)
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                list = tx.set(obj, idx, KObjectType.LIST)
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.List).id shouldBe list
            }
        })
        // Map
        testCases.add(object : TestCase {
            var map: KObjectId? = null

            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                map = tx.set(obj, key, KObjectType.MAP)
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                map = tx.set(obj, idx, KObjectType.MAP)
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Map).id shouldBe map
            }
        })
        // Text
        testCases.add(object : TestCase {
            var text: KObjectId? = null

            override fun set(tx: KTransaction, obj: KObjectId, key: String) {
                text = tx.set(obj, key, KObjectType.TEXT)
            }

            override fun set(tx: KTransaction, obj: KObjectId, idx: Long) {
                text = tx.set(obj, idx, KObjectType.TEXT)
            }

            override fun check(value: KAmValue) {
                (value as KAmValue.Text).id shouldBe text
            }
        })
        return testCases
    }

    @Test
    fun testGetMap() {
        for (testCase in makeTestCases()) {
            val doc = KDocument()
            doc.startTransaction().use { tx ->
                testCase.set(tx, KObjectId.Companion.ROOT, "key")
                testCase.check(tx[KObjectId.Companion.ROOT, "key"]!!)
                testCase.check(doc[KObjectId.Companion.ROOT, "key"]!!)
                tx.commit()
                testCase.check(doc[KObjectId.Companion.ROOT, "key"]!!)
            }
        }
    }

    @Test
    fun testGetList2() {
        for (testCase in makeTestCases()) {
            val doc = KDocument()
            doc.startTransaction().use { tx ->
                tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST).let { listId ->

                    tx.insert(listId, 0, KNewValue.Companion.Null)

                    testCase.let { tc ->
                        tc.set(tx, listId, 0)
                        tc.check(tx[listId, 0]!!)
                        tc.check(doc[listId, 0]!!)
                        tx.commit()

                        tc.check(doc[listId, 0]!!)
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
            val doc = KDocument()
            doc.startTransaction().also { tx ->
                val list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
                tx.insert(list, 0, KNewValue.Companion.Null) // tx[0] = NULL
                testCase.set(tx, list, 0) // tx[0] = 1
                testCase.check(tx[list, 0]!!)
                tx.commit()
                val value = doc[list, 0]
                testCase.check(value!!)
            }
        }
    }
}
