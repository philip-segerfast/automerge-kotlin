package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions

class TestListItems : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var list: KObjectId
    private lateinit var subList: KObjectId
    private lateinit var map: KObjectId
    private lateinit var text: KObjectId
    private lateinit var bytes: ByteArray
    private lateinit var date: Instant

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
    }

    @Test
    fun testListItems() {
        var tx = doc.startTransaction()
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
        val heads = doc.heads

        // Now delete the items we inserted
        tx = doc.startTransaction()
        tx.splice(list, 1, 11, emptyList())

        // Check the current length in the open transaction
        Assertions.assertEquals(1, tx.length(list))
        // Check the current length in the doc with open transaction
        Assertions.assertEquals(1, doc.length(list))

        // Check the current items in the open transaction
        var items = tx.listItems(list)!!
        Assertions.assertEquals(1, (items[0] as KAmValue.Int).value)
        // Check the current items in the doc with open transaction
        items = doc.listItems(list)!!
        Assertions.assertEquals(1, (items[0] as KAmValue.Int).value)

        // Check the length at heads in the open transaction
        Assertions.assertEquals(12, tx.length(list, heads))
        // Check the length at heads in the doc with open transaction
        Assertions.assertEquals(12, doc.length(list, heads))

        // Check the list items at heads in the open transaction
        items = tx.listItems(list, heads)!!
        assertItems(items)

        // Check the list items at heads in the doc with open transaction
        items = doc.listItems(list, heads)!!
        assertItems(items)
        tx.commit()

        // Check the current items in doc with closed transaction
        items = doc.listItems(list)!!
        Assertions.assertEquals(1, (items[0] as KAmValue.Int).value)

        // Check the list items at heads in the doc with closed transaction
        items = doc.listItems(list, heads)!!
        assertItems(items)
    }

    private fun insertListItems(tx: KTransaction) {
        list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)

        tx.insert(list, 0, 1)
        tx.insert(list, 1, KNewValue.Companion.uint(2))
        tx.insert(list, 2, false)
        bytes = "bytes".toByteArray()
        tx.insert(list, 3, bytes)
        date = Clock.System.now()
        tx.insert(list, 4, date)
        tx.insert(list, 5, 1.2)
        tx.insert(list, 6, "somestring")
        tx.insert(list, 7, KNewValue.Companion.Null)
//        tx.insert(list, 8, Counter(10))
        map = tx.insert(list, 9, KObjectType.MAP)
        subList = tx.insert(list, 10, KObjectType.LIST)
        text = tx.insert(list, 11, KObjectType.TEXT)
    }

    private fun <R : KRead?> assertListItems(read: R) {
        Assertions.assertEquals(12, read!!.length(list))
        val items = read.listItems(list)!!
        assertItems(items)
    }

    private fun assertItems(items: List<KAmValue>) {
        Assertions.assertEquals(12, items.size)
        Assertions.assertEquals(1, (items[0] as KAmValue.Int).value)
        Assertions.assertEquals(2, (items[1] as KAmValue.UInt).value)
        Assertions.assertEquals(false, (items[2] as KAmValue.Bool).value)
        Assertions.assertArrayEquals(bytes, (items[3] as KAmValue.Bytes).value)
        Assertions.assertEquals(date, (items[4] as KAmValue.Timestamp).value)
        Assertions.assertEquals(1.2, (items[5] as KAmValue.F64).value)
        Assertions.assertEquals("somestring", (items[6] as KAmValue.Str).value)
        Assertions.assertInstanceOf(
            KAmValue.Null::class.java,
            items[7]
        )
        Assertions.assertEquals(10, (items[8] as KAmValue.Counter).value)
        Assertions.assertEquals(map, (items[9] as KAmValue.Map).id)
        Assertions.assertEquals(subList, (items[10] as KAmValue.List).id)
        Assertions.assertEquals(text, (items[11] as KAmValue.Text).id)
    }
}
