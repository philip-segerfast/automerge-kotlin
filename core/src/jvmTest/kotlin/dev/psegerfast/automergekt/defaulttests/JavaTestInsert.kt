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

class JavaTestInsert : AnnotationSpec() {
    private var doc: Document? = null
    private var tx: Transaction? = null
    private var list: ObjectId? = null

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        tx = doc!!.startTransaction()
        list = tx!!.set(ObjectId.ROOT, "key", ObjectType.LIST)
    }

    @Test
    fun testInsertDoubleInList() {
        tx!!.insert(list, 0, 1.23)
        Assertions.assertEquals(1.23, (doc!![list, 0].get() as AmValue.F64).value)
    }

    @Test
    fun testInsertStringInList() {
        tx!!.insert(list, 0, "something")
        Assertions.assertEquals("something", (doc!![list, 0].get() as AmValue.Str).value)
    }

    @Test
    fun testInsertIntInList() {
        tx!!.insert(list, 0, 10)
        Assertions.assertEquals(10, (doc!![list, 0].get() as AmValue.Int).value)
    }

    @Test
    fun testInsertUingInList() {
        tx!!.insert(list, 0, NewValue.uint(10))
        Assertions.assertEquals(10, (doc!![list, 0].get() as AmValue.UInt).value)
    }

    @Test
    fun testInsertBytesInList() {
        val value = "somebytes".toByteArray()
        tx!!.insert(list, 0, value)
        val result = doc!![list, 0]
        Assertions.assertTrue(result.isPresent)
        Assertions.assertArrayEquals((result.get() as AmValue.Bytes).value, value)
    }

    @Test
    fun testInsertNullInList() {
        tx!!.insert(list, 0, NewValue.NULL)
        Assertions.assertInstanceOf(AmValue.Null::class.java, doc!![list, 0].get())
    }

//    @Test
//    fun testInsertCounterInList() {
//        tx!!.insert(list, 0, Counter(10))
//        Assertions.assertEquals(10, (doc!![list, 0].get() as AmValue.Counter).value)
//    }

    @Test
    fun testInsertDateInList() {
        val now = Date()
        tx!!.insert(list, 0, now)
        Assertions.assertEquals(now, (doc!![list, 0].get() as AmValue.Timestamp).value)
    }

    @Test
    fun testBoolInList() {
        tx!!.insert(list, 0, false)
        tx!!.insert(list, 1, true)
        Assertions.assertEquals(false, (doc!![list, 0].get() as AmValue.Bool).value)
        Assertions.assertEquals(true, (doc!![list, 1].get() as AmValue.Bool).value)
    }

    @Test
    fun testInsertObjInList() {
        val listId = tx!!.insert(list, 0, ObjectType.LIST)
        val textId = tx!!.insert(list, 1, ObjectType.TEXT)
        val mapId = tx!!.insert(list, 2, ObjectType.MAP)
        Assertions.assertEquals(listId, (doc!![list, 0].get() as AmValue.List).id)
        Assertions.assertEquals(textId, (doc!![list, 1].get() as AmValue.Text).id)
        Assertions.assertEquals(mapId, (doc!![list, 2].get() as AmValue.Map).id)
    }
}
