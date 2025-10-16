package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.MapEntry
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Read
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions
import java.util.Date
import java.util.function.Function

internal class JavaTestMapEntries : AnnotationSpec() {
    private var map: ObjectId? = null
    private var list: ObjectId? = null
    private var text: ObjectId? = null
    private var dateValue: Date? = null

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testMapEntries() {
        run { doc: Document -> doc.startTransaction() }
    }

    fun run(createTx: Function<Document, Transaction>) {
        val doc = Document()
        var tx = createTx.apply(doc)
        insertMapEntries(tx)
        assertMapEntries(tx)
        assertMapEntries(doc)
        tx.commit()
        assertMapEntries(doc)

        val heads = doc.heads
        tx = createTx.apply(doc)
        for (key in tx.keys(ObjectId.ROOT).get()) {
            tx.delete(ObjectId.ROOT, key)
        }
        tx[ObjectId.ROOT, "newkey"] = "newvalue"

        assertMapEntriesAfter(tx)
        assertMapEntriesAfter(doc)
        tx.commit()
        assertMapEntriesAfter(doc)

        tx = createTx.apply(doc)
        val txAt = tx.mapEntries(ObjectId.ROOT, heads).get()
        assertMapEntries(txAt)
        val docAtPreCommit = doc.mapEntries(ObjectId.ROOT, heads).get()
        assertMapEntries(docAtPreCommit)
        tx.commit()
        val docAtPostCommit = doc.mapEntries(ObjectId.ROOT, heads).get()
        assertMapEntries(docAtPostCommit)
    }

    fun insertMapEntries(tx: Transaction) {
        map = tx.set(ObjectId.ROOT, "map", ObjectType.MAP)
        list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
        text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)

        tx[ObjectId.ROOT, "int"] = 1
        tx[ObjectId.ROOT, "uint"] = NewValue.uint(1)
        tx[ObjectId.ROOT, "float"] = 1.0
        tx[ObjectId.ROOT, "str"] = "str"
        tx[ObjectId.ROOT, "bytes"] = "bytes".toByteArray()
//        tx[ObjectId.ROOT, "counter"] = Counter(10)
        dateValue = Date()
        tx[ObjectId.ROOT, "date"] = dateValue
        tx[ObjectId.ROOT, "null"] = NewValue.NULL
        tx[ObjectId.ROOT, "bool"] = true
    }

    fun assertMapEntries(read: Read) {
        val result = read.mapEntries(ObjectId.ROOT).get()
        assertMapEntries(result)
    }

    fun assertMapEntries(entries: Array<MapEntry>) {
        val entrysByKey = HashMap<String, MapEntry>()
        for (entry in entries) {
            entrysByKey[entry.key] = entry
        }

        val mapEntry = entrysByKey["map"]
        Assertions.assertEquals(map, (mapEntry!!.value as AmValue.Map).id)

        val listEntry = entrysByKey["list"]
        Assertions.assertEquals(list, (listEntry!!.value as AmValue.List).id)

        val textEntry = entrysByKey["text"]
        Assertions.assertEquals(text, (textEntry!!.value as AmValue.Text).id)

        val intEntry = entrysByKey["int"]
        Assertions.assertEquals(1, (intEntry!!.value as AmValue.Int).value)

        val uintEntry = entrysByKey["uint"]
        Assertions.assertEquals(1, (uintEntry!!.value as AmValue.UInt).value)

        val floatEntry = entrysByKey["float"]
        Assertions.assertEquals(1.0, (floatEntry!!.value as AmValue.F64).value)

        val strEntry = entrysByKey["str"]
        Assertions.assertEquals("str", (strEntry!!.value as AmValue.Str).value)

        val bytesEntry = entrysByKey["bytes"]
        Assertions.assertArrayEquals(
            "bytes".toByteArray(),
            (bytesEntry!!.value as AmValue.Bytes).value
        )

        val counterEntry = entrysByKey["counter"]
        Assertions.assertEquals(10, (counterEntry!!.value as AmValue.Counter).value)

        val dateEntry = entrysByKey["date"]
        Assertions.assertEquals(dateValue, (dateEntry!!.value as AmValue.Timestamp).value)

        val nullEntry = entrysByKey["null"]
        Assertions.assertInstanceOf(AmValue.Null::class.java, nullEntry!!.value)

        val boolEntry = entrysByKey["bool"]
        Assertions.assertEquals(true, (boolEntry!!.value as AmValue.Bool).value)
    }

    fun assertMapEntriesAfter(r: Read) {
        val entries = r.mapEntries(ObjectId.ROOT).get()
        Assertions.assertEquals(1, entries.size)
        Assertions.assertEquals("newkey", entries[0].key)
        Assertions.assertEquals("newvalue", (entries[0].value as AmValue.Str).value)
    }
}
