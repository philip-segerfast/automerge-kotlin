package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Read
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions
import java.util.Collections
import java.util.Date
import kotlin.properties.Delegates

class JavaTestListItems : AnnotationSpec() {
    private var doc: Document? = null
    private var list: ObjectId? = null
    private var subList: ObjectId? = null
    private var map: ObjectId? = null
    private var text: ObjectId? = null
    private var bytes: ByteArray by Delegates.notNull()
    private var date: Date? = null

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
    }

    @Test
    fun testListItems() {
        var tx = doc!!.startTransaction()
        // Insert a bunch of items
        insertListItems(tx)

        // Check we can read them from a doc with open transaction
        assertListItems(doc)
        // Check we can read them from the transaction
        assertListItems(tx)
        tx.commit()
        // Check we can read them from a doc with closed transaction
        assertListItems(doc)

        // Save the heads
        val heads = doc!!.heads

        // Now delete the items we inserted
        tx = doc!!.startTransaction()
        tx.splice(list, 1, 11, Collections.emptyIterator())

        // Check the current length in the open transaction
        Assertions.assertEquals(1, tx.length(list))
        // Check the current length in the doc with open transaction
        Assertions.assertEquals(1, doc!!.length(list))

        // Check the current items in the open transaction
        var items = tx.listItems(list).get()
        Assertions.assertEquals(1, (items[0] as AmValue.Int).value)
        // Check the current items in the doc with open transaction
        items = doc!!.listItems(list).get()
        Assertions.assertEquals(1, (items[0] as AmValue.Int).value)

        // Check the length at heads in the open transaction
        Assertions.assertEquals(12, tx.length(list, heads))
        // Check the length at heads in the doc with open transaction
        Assertions.assertEquals(12, doc!!.length(list, heads))

        // Check the list items at heads in the open transaction
        items = tx.listItems(list, heads).get()
        assertItems(items)

        // Check the list items at heads in the doc with open transaction
        items = doc!!.listItems(list, heads).get()
        assertItems(items)
        tx.commit()

        // Check the current items in doc with closed transaction
        items = doc!!.listItems(list).get()
        Assertions.assertEquals(1, (items[0] as AmValue.Int).value)

        // Check the list items at heads in the doc with closed transaction
        items = doc!!.listItems(list, heads).get()
        assertItems(items)
    }

    fun insertListItems(tx: Transaction) {
        list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)

        tx.insert(list, 0, 1)
        tx.insert(list, 1, NewValue.uint(2))
        tx.insert(list, 2, false)
        bytes = "bytes".toByteArray()
        tx.insert(list, 3, bytes)
        date = Date()
        tx.insert(list, 4, date)
        tx.insert(list, 5, 1.2)
        tx.insert(list, 6, "somestring")
        tx.insert(list, 7, NewValue.NULL)
//        tx.insert(list, 8, Counter(10))
        map = tx.insert(list, 9, ObjectType.MAP)
        subList = tx.insert(list, 10, ObjectType.LIST)
        text = tx.insert(list, 11, ObjectType.TEXT)
    }

    fun <R : Read?> assertListItems(read: R) {
        Assertions.assertEquals(12, read!!.length(list))
        val items = read.listItems(list).get()
        assertItems(items)
    }

    fun assertItems(items: Array<AmValue>) {
        Assertions.assertEquals(12, items.size)
        Assertions.assertEquals(1, (items[0] as AmValue.Int).value)
        Assertions.assertEquals(2, (items[1] as AmValue.UInt).value)
        Assertions.assertEquals(false, (items[2] as AmValue.Bool).value)
        Assertions.assertArrayEquals(bytes, (items[3] as AmValue.Bytes).value)
        Assertions.assertEquals(date, (items[4] as AmValue.Timestamp).value)
        Assertions.assertEquals(1.2, (items[5] as AmValue.F64).value)
        Assertions.assertEquals("somestring", (items[6] as AmValue.Str).value)
        Assertions.assertInstanceOf(
            AmValue.Null::class.java,
            items[7]
        )
        Assertions.assertEquals(10, (items[8] as AmValue.Counter).value)
        Assertions.assertEquals(map, (items[9] as AmValue.Map).id)
        Assertions.assertEquals(subList, (items[10] as AmValue.List).id)
        Assertions.assertEquals(text, (items[11] as AmValue.Text).id)
    }
}
