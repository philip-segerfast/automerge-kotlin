package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions

class TestSplice : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var tx: KTransaction
    private lateinit var list: KObjectId

    fun interface InsertedAssertions {
        fun assertInserted(elem1: Any, elem2: Any)
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
        tx = doc.startTransaction()
        list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
    }

    @AfterEach
    fun tearDown() {
        tx.close()
        doc.free()
    }

    private fun testSplice2(val1: KNewValue, val2: KNewValue, assertions: InsertedAssertions) {
        println("Initial state")
        println(tx.treeString())

        println("Insert 1 at index 0")
        tx.insert(list, 0, 1)
        println(tx.treeString())

        println("Insert 2 at index 1")
        tx.insert(list, 1, 2)
        println(tx.treeString())

        println("Insert 3 at index 2")
        tx.insert(list, 2, 3)
        println(tx.treeString())

        println("Splice $val1 and $val2 into index 1, deleting one value.")
        tx.splice(list, 1, 1, listOf(val1, val2))
        println(tx.treeString())
        Assertions.assertEquals(1L, (tx[list, 0] as KAmValue.Int).value)

        val elem1 = tx[list, 1]
        val elem2 = tx[list, 2]
        println("Value at index 1: $elem1")
        println("Value at index 2: $elem2")

        assertions.assertInserted(elem1!!, elem2!!)

        Assertions.assertEquals(3L, (tx[list, 3] as KAmValue.Int).value)
    }

    @Test
    fun testSpliceInt() {
        testSplice2(
            KNewValue.Companion.integer(4),
            KNewValue.Companion.integer(5)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(4L, (elem1 as KAmValue.Int).value)
            Assertions.assertEquals(5L, (elem2 as KAmValue.Int).value)
        }
    }

    @Test
    fun testSpliceUint() {
        testSplice2(
            KNewValue.Companion.uint(1),
            KNewValue.Companion.uint(2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(1, (elem1 as KAmValue.UInt).value)
            Assertions.assertEquals(2, (elem2 as KAmValue.UInt).value)
        }
    }

    @Test
    fun testSpliceDouble() {
        testSplice2(
            KNewValue.Companion.f64(4.0),
            KNewValue.Companion.f64(5.0)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(4.0, (elem1 as KAmValue.F64).value)
            Assertions.assertEquals(5.0, (elem2 as KAmValue.F64).value)
        }
    }

    @Test
    fun testSpliceBytes() {
        testSplice2(
            KNewValue.Companion.bytes("4".toByteArray()), KNewValue.Companion.bytes("5".toByteArray())
        ) { elem1: Any, elem2: Any ->
            Assertions.assertArrayEquals("4".toByteArray(), (elem1 as KAmValue.Bytes).value)
            Assertions.assertArrayEquals("5".toByteArray(), (elem2 as KAmValue.Bytes).value)
        }
    }

    @Test
    fun testInsertBool() {
        testSplice2(
            KNewValue.Companion.bool(true),
            KNewValue.Companion.bool(false)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(true, (elem1 as KAmValue.Bool).value)
            Assertions.assertEquals(false, (elem2 as KAmValue.Bool).value)
        }
    }

    @Test
    fun testInsertNull() {
        testSplice2(KNewValue.Companion.Null, KNewValue.Companion.Null) { elem1: Any?, elem2: Any? ->
            Assertions.assertInstanceOf(
                KAmValue.Null::class.java, elem1
            )
            Assertions.assertInstanceOf(
                KAmValue.Null::class.java,
                elem2
            )
        }
    }

    @Test
    fun testInsertString() {
        testSplice2(
            KNewValue.Companion.str("4"),
            KNewValue.Companion.str("5")
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals("4", (elem1 as KAmValue.Str).value)
            Assertions.assertEquals("5", (elem2 as KAmValue.Str).value)
        }
    }

    @Test
    fun testSpliceDate() {
        val d1 = Clock.System.now()
        val d2 = Clock.System.now()
        testSplice2(
            KNewValue.Companion.timestamp(d1),
            KNewValue.Companion.timestamp(d2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(d1, (elem1 as KAmValue.Timestamp).value)
            Assertions.assertEquals(d2, (elem2 as KAmValue.Timestamp).value)
        }
    }

    @Test
    fun testSpliceCounter() {
        testSplice2(
            KNewValue.Companion.counter(1),
            KNewValue.Companion.counter(2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(1, (elem1 as KAmValue.Counter).value)
            Assertions.assertEquals(2, (elem2 as KAmValue.Counter).value)
        }
    }
} // Check that committing the transaction clears KDocument.transactionPtr
