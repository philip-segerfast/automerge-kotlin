package dev.psegerfast.automergekt.defaulttests

import dev.psegerfast.automergekotlin.core.toCommon
import dev.psegerfast.automergekotlin.core.treeString
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

class JavaTestSetList : AnnotationSpec() {
    private lateinit var doc: Document
    private lateinit var tx: Transaction
    private lateinit var list: ObjectId

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        tx = doc.startTransaction()
        list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
        tx.insert(list, 0, "something")
    }

    @Test
    fun testSetDoubleInList() {
        tx[list, 0] = 1.23
        Assertions.assertEquals(1.23, (doc[list, 0].get() as AmValue.F64).value)
    }

    @Test
    suspend fun testSetIntInList() {
        println("Transaction before")
        println(tx.toCommon().treeString())
        println("Document before")
        println(doc.toCommon().treeString())

        tx[list, 0] = 123

        println("Transaction after")
        println(tx.toCommon().treeString())
        println("Document after")
        println(doc.toCommon().treeString())

        Assertions.assertEquals(123, (doc[list, 0].get() as AmValue.Int).value)
    }

    @Test
    fun testSetUintInList() {
        tx[list, 0] = NewValue.uint(123)
        Assertions.assertEquals(123, (doc[list, 0].get() as AmValue.UInt).value)
        Assertions.assertInstanceOf(AmValue.UInt::class.java, doc[list, 0].get())
    }

    @Test
    fun testSetStringInList() {
        tx[list, 0] = "hello"
        Assertions.assertEquals("hello", (doc[list, 0].get() as AmValue.Str).value)
    }

    @Test
    fun testSetBytesInList() {
        val value = "some bytes".toByteArray()
        tx[list, 0] = value
        Assertions.assertArrayEquals(value, (doc[list, 0].get() as AmValue.Bytes).value)
    }

    @Test
    fun testSetBooleanInList() {
        tx[list, 0] = true
        Assertions.assertEquals(true, (doc[list, 0].get() as AmValue.Bool).value)
        tx[list, 0] = false
        Assertions.assertEquals(false, (doc[list, 0].get() as AmValue.Bool).value)
    }

    @Test
    fun testSetDateInList() {
        val date = Date()
        tx[list, 0] = date
        Assertions.assertEquals(date, (doc[list, 0].get() as AmValue.Timestamp).value)
    }

//    @Test
//    fun testSetCounterInList() {
//        tx[list, 0] = Counter(10)
//        Assertions.assertEquals(10, (doc[list, 0].get() as AmValue.Counter).value)
//    }

    @Test
    fun testSetNullInList() {
        tx[list, 0] = NewValue.NULL
        Assertions.assertInstanceOf(AmValue.Null::class.java, doc[list, 0].get())
    }

    @Test
    fun setObjectInList() {
        val listId = tx.set(list, 0, ObjectType.LIST)
        Assertions.assertEquals(listId, (doc[list, 0].get() as AmValue.List).id)
        val textId = tx.set(list, 0, ObjectType.TEXT)
        Assertions.assertEquals(textId, (doc[list, 0].get() as AmValue.Text).id)
        val mapId = tx.set(list, 0, ObjectType.MAP)
        Assertions.assertEquals(mapId, (doc[list, 0].get() as AmValue.Map).id)
    }
}
