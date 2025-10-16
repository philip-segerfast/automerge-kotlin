package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import org.automerge.Document
import org.junit.jupiter.api.Assertions
import kotlin.text.set

class TestSetList : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var tx: KTransaction
    private lateinit var list: KObjectId

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        println("----------------- Creating new document")
        doc = KDocument()
        tx = doc.startTransaction()
        list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
        tx.insert(list, 0, "something")

        val x = Document()
        val tx2 = x.startTransaction()
        tx2.rollback()
    }

    @AfterEach
    fun teardown() {
        println("----------------- Teardown")
        println("BEFORE Tx index 0: ${ tx[list, 0] }")
        println("BEFORE Doc index 0: ${ doc[list, 0] }")
        tx.commit()
        println("AFTER Tx index 0: ${ tx[list, 0] }")
        println("AFTER Doc index 0: ${ doc[list, 0] }")
        tx.close()
        doc.free()
    }

    @Test
    fun testSetDoubleInList() {
        tx[list, 0] = 1.23
        Assertions.assertEquals(1.23, (doc[list, 0] as KAmValue.F64).value)
    }

    @Test
    suspend fun testSetIntInList() = coroutineScope {
        println("Transaction before")
        println(tx.treeString())
        println("Document before")
        println(doc.treeString())

        tx[list, 0] = 123

        println("Transaction after")
        println(tx.treeString())
        println("Document after")
        println(doc.treeString())
//
//        val docList = doc.listItems(list)
//        val txList = tx.listItems(list)
//        println("Doc list: $docList")
//        println("Tx list: $txList")
//
//        repeat(20) {
//            println("--------")
//            println("Doc value: ${ doc[list, 0] }")
//            println("Tx value: ${ tx[list, 0] }")
//            delay(500.milliseconds)
//        }

        Assertions.assertEquals(123, (doc[list, 0] as KAmValue.Int).value)
    }

    @Test
    fun testSetUintInList() {
        tx[list, 0] = KNewValue.Companion.uint(123)
        Assertions.assertEquals(123, (doc[list, 0] as KAmValue.UInt).value)
        Assertions.assertInstanceOf(KAmValue.UInt::class.java, doc[list, 0])
    }

    @Test
    fun testSetStringInList() {
        tx[list, 0] = "hello"
        Assertions.assertEquals("hello", (doc[list, 0] as KAmValue.Str).value)
    }

    @Test
    fun testSetBytesInList() {
        val value = "some bytes".toByteArray()
        tx[list, 0] = value
        Assertions.assertArrayEquals(value, (doc[list, 0] as KAmValue.Bytes).value)
    }

    @Test
    fun testSetBooleanInList() {
        tx[list, 0] = true
        Assertions.assertEquals(true, (doc[list, 0] as KAmValue.Bool).value)
        tx[list, 0] = false
        Assertions.assertEquals(false, (doc[list, 0] as KAmValue.Bool).value)
    }

    @Test
    fun testSetDateInList() {
        val date = Clock.System.now()
        tx[list, 0] = date
        Assertions.assertEquals(date, (doc[list, 0] as KAmValue.Timestamp).value)
    }

//    @Test
//    fun testSetCounterInList() {
//        tx[list, 0] = Counter(10)
//        Assertions.assertEquals(10, (doc[list, 0]!! as KAmValue.Counter).value)
//    }

    @Test
    fun testSetNullInList() {
        tx[list, 0] = KNewValue.Companion.Null
        Assertions.assertInstanceOf(KAmValue.Null::class.java, doc[list, 0])
    }

    @Test
    fun setObjectInList() {
        val listId = tx.set(list, 0, KObjectType.LIST)
        Assertions.assertEquals(listId, (doc[list, 0] as KAmValue.List).id)
        val textId = tx.set(list, 0, KObjectType.TEXT)
        Assertions.assertEquals(textId, (doc[list, 0] as KAmValue.Text).id)
        val mapId = tx.set(list, 0, KObjectType.MAP)
        Assertions.assertEquals(mapId, (doc[list, 0] as KAmValue.Map).id)
    }
}
