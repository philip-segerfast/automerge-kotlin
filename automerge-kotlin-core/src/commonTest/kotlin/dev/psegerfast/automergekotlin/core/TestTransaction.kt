package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import kotlin.text.set

class TestTransaction : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var tx: KTransaction

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
        tx = doc.startTransaction()
    }

    @Test
    fun commitKTransaction() {
        // Check that committing the transaction clears KDocument.transactionPtr
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.commit()
        tx = doc.startTransaction()
        tx.set(KObjectId.Companion.ROOT, "key", 4.56)
        tx.commit()
        val result = doc[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(4.56, (result as KAmValue.F64).value)
    }

    @Test
    fun exceptionCaughtWhenSetting() {
        // Create an object in another document, then use that object in this document,
        // yielding an error in Rust
        // then check the error is caught and thrown as an AutomergeException.
        val otherDoc = KDocument()
        val otherObj: KObjectId
        otherDoc.startTransaction().use { tx ->
            otherObj = tx.set(KObjectId.Companion.ROOT, "key", KObjectType.MAP)
        }
        // Close the already running transaction from `setup`
        tx.commit()
        Assertions.assertThrows(
            RuntimeException::class.java
        ) {
            doc.startTransaction().use { tx ->
                tx[otherObj, "key"] = 1.23
            }
        }
    }

    @Test
    fun testRollBackRemovesOps() {
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.rollback()
        Assertions.assertTrue(doc[KObjectId.Companion.ROOT, "key"] == null)
    }

    @Test
    fun testRollbackClearsKTransaction() {
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.rollback()
        doc.startTransaction()
    }

    @Test
    fun testCloseWithoutCommitRollsback() {
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.close()
        Assertions.assertTrue(doc[KObjectId.Companion.ROOT, "key"] == null)
        // Check we can start a new transaction because the last one was closed
        doc.startTransaction()
    }

    @Test
    fun testCloseAfterCommitIsNoOp() {
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.commit()
        tx.close()
        Assertions.assertEquals(1.23, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.F64).value)
        // Check we can start a new transaction because the last one was closed
        doc.startTransaction()
    }

    // Basic tests for setting properties in maps
    @Test
    fun setDoubleInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        tx.commit()
        Assertions.assertEquals(1.23, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.F64).value)
    }

    @Test
    fun setBytesInMap() {
        val somebytes = "some bytes".toByteArray()
        tx[KObjectId.Companion.ROOT, "bytes"] = somebytes
        tx.commit()
        Assertions.assertArrayEquals(
            somebytes,
            (doc[KObjectId.Companion.ROOT, "bytes"] as KAmValue.Bytes).value
        )
    }

    @Test
    fun setStringInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = "some string"
        Assertions.assertEquals(
            "some string",
            (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.Str).value
        )
    }

    @Test
    fun setIntInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = 10
        Assertions.assertEquals(10, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.Int).value)
    }

    @Test
    fun setUintInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = KNewValue.Companion.uint(10)
        Assertions.assertEquals(10, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.UInt).value)
    }

    @Test
    fun setBoolInMap() {
        tx[KObjectId.Companion.ROOT, "false"] = false
        tx[KObjectId.Companion.ROOT, "true"] = true
        Assertions.assertEquals(false, (doc[KObjectId.Companion.ROOT, "false"] as KAmValue.Bool).value)
        Assertions.assertEquals(true, (doc[KObjectId.Companion.ROOT, "true"] as KAmValue.Bool).value)
    }

    @Test
    fun setNullInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = KNewValue.Companion.Null
        Assertions.assertInstanceOf(KAmValue.Null::class.java, doc[KObjectId.Companion.ROOT, "key"])
    }

//    @Test
//    fun setCounterInMap() {
//        tx[KObjectId.ROOT, "counter"] = Counter(10)
//        Assertions.assertEquals(
//            10,
//            (doc[KObjectId.ROOT, "counter"] as KAmValue.Counter).value
//        )
//    }

    @Test
    fun setDateInMap() {
        val date = Clock.System.now()
        tx[KObjectId.Companion.ROOT, "key"] = date
        val actual = (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.Timestamp).value
        // Compare milliseconds because of rounding errors
        Assertions.assertEquals(
            date.toEpochMilliseconds(),
            actual.toEpochMilliseconds()
        )
    }

    @Test
    fun getNone() {
        Assertions.assertTrue(
            doc[KObjectId.Companion.ROOT, "nonexistent"] == null
        )
    }

    // Test for creating objects
    @Test
    fun putMapInMap() {
        val nested = tx.set(KObjectId.Companion.ROOT, "nested", KObjectType.MAP)
        tx[nested, "key"] = 1.23
        tx.commit()
        Assertions.assertEquals(nested, (doc[KObjectId.Companion.ROOT, "nested"] as KAmValue.Map).id)
    }

    @Test
    fun putListInMap() {
        val nested = tx.set(KObjectId.Companion.ROOT, "nested", KObjectType.LIST)
        Assertions.assertEquals(nested, (doc[KObjectId.Companion.ROOT, "nested"] as KAmValue.List).id)
    }

    @Test
    fun testGetInListInTx() {
        val nested = tx.set(KObjectId.Companion.ROOT, "nested", KObjectType.LIST)
        tx.insert(nested, 0, 123)
//        Assertions.assertEquals(123, (doc[nested, 0] as KAmValue.Int).value)
        Assertions.assertEquals(123, (tx[nested, 0] as KAmValue.Int).value)
        tx.commit()
        Assertions.assertEquals(123, (doc[nested, 0] as KAmValue.Int).value)
    }

    @Test
    fun testGetInMapInTx() {
        tx[KObjectId.Companion.ROOT, "key"] = 123
        Assertions.assertEquals(123, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.Int).value)
        Assertions.assertEquals(123, (tx[KObjectId.Companion.ROOT, "key"] as KAmValue.Int).value)
        tx.commit()
        Assertions.assertEquals(123, (doc[KObjectId.Companion.ROOT, "key"] as KAmValue.Int).value)
    }
}
