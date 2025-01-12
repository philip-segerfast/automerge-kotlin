package dev.psegerfast.automergekt.defaulttests

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

class JavaTestTransaction : AnnotationSpec() {
    private lateinit var doc: Document
    private lateinit var tx: Transaction

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        tx = doc.startTransaction()
    }

    @Test
    fun commitTransaction() {
        // Check that committing the transaction clears Document.transactionPtr
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.commit()
        tx = doc.startTransaction()
        tx.set(ObjectId.ROOT, "key", 4.56)
        tx.commit()
        val result = doc[ObjectId.ROOT, "key"]
        Assertions.assertEquals(4.56, (result.get() as AmValue.F64).value)
    }

    @Test
    fun exceptionCaughtWhenSetting() {
        // Create an object in another document, then use that object in this document,
        // yielding an error in Rust
        // then check the error is caught and thrown as an AutomergeException.
        val otherDoc = Document()
        val otherObj: ObjectId
        otherDoc.startTransaction().use { tx ->
            otherObj = tx.set(ObjectId.ROOT, "key", ObjectType.MAP)
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
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.rollback()
        Assertions.assertTrue(doc[ObjectId.ROOT, "key"].isEmpty)
    }

    @Test
    fun testRollbackClearsTransaction() {
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.rollback()
        doc.startTransaction()
    }

    @Test
    fun testCloseWithoutCommitRollsback() {
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.close()
        Assertions.assertTrue(doc[ObjectId.ROOT, "key"].isEmpty)
        // Check we can start a new transaction because the last one was closed
        doc.startTransaction()
    }

    @Test
    fun testCloseAfterCommitIsNoOp() {
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.commit()
        tx.close()
        Assertions.assertEquals(1.23, (doc[ObjectId.ROOT, "key"].get() as AmValue.F64).value)
        // Check we can start a new transaction because the last one was closed
        doc.startTransaction()
    }

    // Basic tests for setting properties in maps
    @Test
    fun setDoubleInMap() {
        tx[ObjectId.ROOT, "key"] = 1.23
        tx.commit()
        Assertions.assertEquals(1.23, (doc[ObjectId.ROOT, "key"].get() as AmValue.F64).value)
    }

    @Test
    fun setBytesInMap() {
        val somebytes = "some bytes".toByteArray()
        tx[ObjectId.ROOT, "bytes"] = somebytes
        tx.commit()
        Assertions.assertArrayEquals(
            somebytes,
            (doc[ObjectId.ROOT, "bytes"].get() as AmValue.Bytes).value
        )
    }

    @Test
    fun setStringInMap() {
        tx[ObjectId.ROOT, "key"] = "some string"
        Assertions.assertEquals(
            "some string",
            (doc[ObjectId.ROOT, "key"].get() as AmValue.Str).value
        )
    }

    @Test
    fun setIntInMap() {
        tx[ObjectId.ROOT, "key"] = 10
        Assertions.assertEquals(10, (doc[ObjectId.ROOT, "key"].get() as AmValue.Int).value)
    }

    @Test
    fun setUintInMap() {
        tx[ObjectId.ROOT, "key"] = NewValue.uint(10)
        Assertions.assertEquals(10, (doc[ObjectId.ROOT, "key"].get() as AmValue.UInt).value)
    }

    @Test
    fun setBoolInMap() {
        tx[ObjectId.ROOT, "false"] = false
        tx[ObjectId.ROOT, "true"] = true
        Assertions.assertEquals(false, (doc[ObjectId.ROOT, "false"].get() as AmValue.Bool).value)
        Assertions.assertEquals(true, (doc[ObjectId.ROOT, "true"].get() as AmValue.Bool).value)
    }

    @Test
    fun setNullInMap() {
        tx[ObjectId.ROOT, "key"] = NewValue.NULL
        Assertions.assertInstanceOf(AmValue.Null::class.java, doc[ObjectId.ROOT, "key"].get())
    }

//    @Test
//    fun setCounterInMap() {
//        tx[ObjectId.ROOT, "counter"] = Counter(10)
//        Assertions.assertEquals(
//            10,
//            (doc[ObjectId.ROOT, "counter"].get() as AmValue.Counter).value
//        )
//    }

    @Test
    fun setDateInMap() {
        val date = Date()
        tx[ObjectId.ROOT, "key"] = date
        Assertions.assertEquals(
            date,
            (doc[ObjectId.ROOT, "key"].get() as AmValue.Timestamp).value
        )
    }

    @Test
    fun getNone() {
        Assertions.assertTrue(
            doc[ObjectId.ROOT, "nonexistent"].isEmpty
        )
    }

    // Test for creating objects
    @Test
    fun putMapInMap() {
        val nested = tx.set(ObjectId.ROOT, "nested", ObjectType.MAP)
        tx[nested, "key"] = 1.23
        tx.commit()
        Assertions.assertEquals(nested, (doc[ObjectId.ROOT, "nested"].get() as AmValue.Map).id)
    }

    @Test
    fun putListInMap() {
        val nested = tx.set(ObjectId.ROOT, "nested", ObjectType.LIST)
        Assertions.assertEquals(nested, (doc[ObjectId.ROOT, "nested"].get() as AmValue.List).id)
    }

    @Test
    fun testGetInListInTx() {
        val nested = tx.set(ObjectId.ROOT, "nested", ObjectType.LIST)
        tx.insert(nested, 0, 123)
//        Assertions.assertEquals(123, (doc[nested, 0].get() as AmValue.Int).value)
        Assertions.assertEquals(123, (tx[nested, 0].get() as AmValue.Int).value)
        tx.commit()
        Assertions.assertEquals(123, (doc[nested, 0].get() as AmValue.Int).value)
    }

    @Test
    fun testGetInMapInTx() {
        tx[ObjectId.ROOT, "key"] = 123
        Assertions.assertEquals(123, (doc[ObjectId.ROOT, "key"].get() as AmValue.Int).value)
        Assertions.assertEquals(123, (tx[ObjectId.ROOT, "key"].get() as AmValue.Int).value)
        tx.commit()
        Assertions.assertEquals(123, (doc[ObjectId.ROOT, "key"].get() as AmValue.Int).value)
    }
}
