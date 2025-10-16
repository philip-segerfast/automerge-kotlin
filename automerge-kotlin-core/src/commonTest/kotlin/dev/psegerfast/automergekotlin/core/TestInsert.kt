package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions

class TestInsert : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var tx: KTransaction
    private lateinit var list: KObjectId

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
        tx = doc.startTransaction()
        list = tx.set(KObjectId.Companion.ROOT, "key", KObjectType.LIST)
    }

    @Test
    fun testInsertDoubleInList() {
        tx.insert(list, 0, 1.23)
        Assertions.assertEquals(1.23, (doc[list, 0] as KAmValue.F64).value)
    }

    @Test
    fun testInsertStringInList() {
        tx.insert(list, 0, "something")
        Assertions.assertEquals("something", (doc[list, 0] as KAmValue.Str).value)
    }

    @Test
    fun testInsertIntInList() {
        tx.insert(list, 0, 10)
        Assertions.assertEquals(10, (doc[list, 0] as KAmValue.Int).value)
    }

    @Test
    fun testInsertUingInList() {
        tx.insert(list, 0, KNewValue.Companion.uint(10))
        Assertions.assertEquals(10, (doc[list, 0] as KAmValue.UInt).value)
    }

    @Test
    fun testInsertBytesInList() {
        val value = "somebytes".toByteArray()
        tx.insert(list, 0, value)
        val result = doc[list, 0]
        Assertions.assertTrue(result != null)
        Assertions.assertArrayEquals((result as KAmValue.Bytes).value, value)
    }

    @Test
    fun testInsertNullInList() {
        tx.insert(list, 0, KNewValue.Companion.Null)
        Assertions.assertInstanceOf(KAmValue.Null::class.java, doc[list, 0])
    }

//    @Test
//    fun testInsertCounterInList() {
//        tx.insert(list, 0, Counter(10))
//        Assertions.assertEquals(10, (doc[list, 0].get() as KAmValue.Counter).value)
//    }

    @Test
    fun testInsertDateInList() {
        val now = Clock.System.now()
        tx.insert(list, 0, now)
        Assertions.assertEquals(now, (doc[list, 0] as KAmValue.Timestamp).value)
    }

    @Test
    fun testBoolInList() {
        tx.insert(list, 0, false)
        tx.insert(list, 1, true)
        Assertions.assertEquals(false, (doc[list, 0] as KAmValue.Bool).value)
        Assertions.assertEquals(true, (doc[list, 1] as KAmValue.Bool).value)
    }

    @Test
    fun testInsertObjInList() {
        val listId = tx.insert(list, 0, KObjectType.LIST)
        val textId = tx.insert(list, 1, KObjectType.TEXT)
        val mapId = tx.insert(list, 2, KObjectType.MAP)
        Assertions.assertEquals(listId, (doc[list, 0] as KAmValue.List).id)
        Assertions.assertEquals(textId, (doc[list, 1] as KAmValue.Text).id)
        Assertions.assertEquals(mapId, (doc[list, 2] as KAmValue.Map).id)
    }
}
