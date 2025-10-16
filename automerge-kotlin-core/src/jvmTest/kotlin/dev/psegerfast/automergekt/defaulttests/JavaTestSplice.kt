package dev.psegerfast.automergekt.defaulttests

import dev.psegerfast.automergekotlin.core.toCommon
import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions
import java.util.Date

class JavaTestSplice : AnnotationSpec() {
    private lateinit var doc: Document
    private lateinit var tx: Transaction
    private lateinit var list: ObjectId

    fun interface InsertedAssertions {
        fun assertInserted(elem1: Any, elem2: Any)
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        tx = doc.startTransaction()
        list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
    }

    @AfterEach
    fun tearDown() {
        tx.close()
        doc.free()
    }

    private fun Transaction.treeString() = toCommon().let { "REMOVE THIS CODE" }
    private fun Document.treeString() = toCommon().let { "REMOVE THIS CODE" }

    private fun testSplice2(val1: NewValue, val2: NewValue, assertions: InsertedAssertions) {
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

        println("Splice $val1 and $val2 at index 1, deleting one value.")
        val toInsert = arrayOf(val1, val2)
        tx.splice(list, 1, 1, listOf(*toInsert).iterator())
        println(tx.treeString())

        tx[list, 0].let { value ->
            println("TX Value at index 0: $value")
            println("Doc Value at index 0: ${doc[list, 0]}")

            println("Assert value == 1L")
            Assertions.assertEquals(1L, (value.get() as AmValue.Int).value)
        }

        val elem1 = tx[list, 1]
        val elem2 = tx[list, 2]
        println("Value at index 1: $elem1")
        println("Value at index 2: $elem2")

        assertions.assertInserted(elem1.get(), elem2.get())

        Assertions.assertEquals(3L, (tx[list, 3].get() as AmValue.Int).value)
    }

    @Test
    fun testSpliceInt() {
        testSplice2(
            NewValue.integer(4),
            NewValue.integer(5)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(4L, (elem1 as AmValue.Int).value)
            Assertions.assertEquals(5L, (elem2 as AmValue.Int).value)
        }
    }

    @Test
    fun testSpliceUint() {
        testSplice2(
            NewValue.uint(1),
            NewValue.uint(2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(1, (elem1 as AmValue.UInt).value)
            Assertions.assertEquals(2, (elem2 as AmValue.UInt).value)
        }
    }

    @Test
    fun testSpliceDouble() {
        testSplice2(
            NewValue.f64(4.0),
            NewValue.f64(5.0)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(4.0, (elem1 as AmValue.F64).value)
            Assertions.assertEquals(5.0, (elem2 as AmValue.F64).value)
        }
    }

    @Test
    fun testSpliceBytes() {
        testSplice2(NewValue.bytes("4".toByteArray()), NewValue.bytes("5".toByteArray())
        ) { elem1: Any, elem2: Any ->
            Assertions.assertArrayEquals("4".toByteArray(), (elem1 as AmValue.Bytes).value)
            Assertions.assertArrayEquals("5".toByteArray(), (elem2 as AmValue.Bytes).value)
        }
    }

    @Test
    fun testInsertBool() {
        testSplice2(
            NewValue.bool(true),
            NewValue.bool(false)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(true, (elem1 as AmValue.Bool).value)
            Assertions.assertEquals(false, (elem2 as AmValue.Bool).value)
        }
    }

    @Test
    fun testInsertNull() {
        testSplice2(NewValue.NULL, NewValue.NULL) { elem1: Any?, elem2: Any? ->
            Assertions.assertInstanceOf(
                AmValue.Null::class.java, elem1
            )
            Assertions.assertInstanceOf(
                AmValue.Null::class.java,
                elem2
            )
        }
    }

    @Test
    fun testInsertString() {
        testSplice2(
            NewValue.str("4"),
            NewValue.str("5")
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals("4", (elem1 as AmValue.Str).value)
            Assertions.assertEquals("5", (elem2 as AmValue.Str).value)
        }
    }

    @Test
    fun testSpliceDate() {
        val d1 = Date()
        val d2 = Date()
        testSplice2(
            NewValue.timestamp(d1),
            NewValue.timestamp(d2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(d1, (elem1 as AmValue.Timestamp).value)
            Assertions.assertEquals(d2, (elem2 as AmValue.Timestamp).value)
        }
    }

    @Test
    fun testSpliceCounter() {
        testSplice2(
            NewValue.counter(1),
            NewValue.counter(2)
        ) { elem1: Any, elem2: Any ->
            Assertions.assertEquals(1, (elem1 as AmValue.Counter).value)
            Assertions.assertEquals(2, (elem2 as AmValue.Counter).value)
        }
    }
} // Check that committing the transaction clears Document.transactionPtr
