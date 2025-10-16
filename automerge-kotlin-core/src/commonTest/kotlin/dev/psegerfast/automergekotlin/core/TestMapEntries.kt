package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions

internal class TestMapEntries : AnnotationSpec() {
    private lateinit var map: KObjectId
    private lateinit var list: KObjectId
    private lateinit var text: KObjectId
    private lateinit var dateValue: Instant

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testMapEntries() {
        run { doc: KDocument -> doc.startTransaction() }
    }

    private fun run(createTx: (KDocument) -> KTransaction) {
        val doc = KDocument()
        var tx = createTx(doc)
        insertMapEntries(tx)
        assertMapEntries(tx)
        assertMapEntries(doc)
        tx.commit()
        assertMapEntries(doc)

        val heads = doc.heads
        tx = createTx(doc)
        for (key in tx.keys(KObjectId.Companion.ROOT)!!) {
            tx.delete(KObjectId.Companion.ROOT, key)
        }
        tx[KObjectId.Companion.ROOT, "newkey"] = "newvalue"

        assertMapEntriesAfter(tx)
        assertMapEntriesAfter(doc)
        tx.commit()
        assertMapEntriesAfter(doc)

        tx = createTx(doc)
        val txAt = tx.mapEntries(KObjectId.Companion.ROOT, heads)!!
        assertMapEntries(txAt)
        val docAtPreCommit = doc.mapEntries(KObjectId.Companion.ROOT, heads)!!
        assertMapEntries(docAtPreCommit)
        tx.commit()
        val docAtPostCommit = doc.mapEntries(KObjectId.Companion.ROOT, heads)!!
        assertMapEntries(docAtPostCommit)
    }

    private fun insertMapEntries(tx: KTransaction) {
        map = tx.set(KObjectId.Companion.ROOT, "map", KObjectType.MAP)
        list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
        text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)

        tx[KObjectId.Companion.ROOT, "int"] = 1
        tx[KObjectId.Companion.ROOT, "uint"] = KNewValue.Companion.uint(1)
        tx[KObjectId.Companion.ROOT, "float"] = 1.0
        tx[KObjectId.Companion.ROOT, "str"] = "str"
        tx[KObjectId.Companion.ROOT, "bytes"] = "bytes".toByteArray()
//        tx[KObjectId.ROOT, "counter"] = Counter(10)
        dateValue = Clock.System.now()
        tx[KObjectId.Companion.ROOT, "date"] = dateValue
        tx[KObjectId.Companion.ROOT, "null"] = KNewValue.Companion.Null
        tx[KObjectId.Companion.ROOT, "bool"] = true
    }

    private fun assertMapEntries(read: KReadable) {
        val result = read.mapEntries(KObjectId.Companion.ROOT)!!
        assertMapEntries(result)
    }

    private fun assertMapEntries(entries: List<KMapEntry>) {
        val entriesByKey = HashMap<String, KMapEntry>()
        for (entry in entries) {
            entriesByKey[entry.key] = entry
        }

        val mapEntry = entriesByKey["map"]
        Assertions.assertEquals(map, (mapEntry!!.value as KAmValue.Map).id)

        val listEntry = entriesByKey["list"]
        Assertions.assertEquals(list, (listEntry!!.value as KAmValue.List).id)

        val textEntry = entriesByKey["text"]
        Assertions.assertEquals(text, (textEntry!!.value as KAmValue.Text).id)

        val intEntry = entriesByKey["int"]
        Assertions.assertEquals(1, (intEntry!!.value as KAmValue.Int).value)

        val uintEntry = entriesByKey["uint"]
        Assertions.assertEquals(1, (uintEntry!!.value as KAmValue.UInt).value)

        val floatEntry = entriesByKey["float"]
        Assertions.assertEquals(1.0, (floatEntry!!.value as KAmValue.F64).value)

        val strEntry = entriesByKey["str"]
        Assertions.assertEquals("str", (strEntry!!.value as KAmValue.Str).value)

        val bytesEntry = entriesByKey["bytes"]
        Assertions.assertArrayEquals(
            "bytes".toByteArray(),
            (bytesEntry!!.value as KAmValue.Bytes).value
        )

        val counterEntry = entriesByKey["counter"]
        Assertions.assertEquals(10, (counterEntry!!.value as KAmValue.Counter).value)

        val dateEntry = entriesByKey["date"]
        Assertions.assertEquals(dateValue, (dateEntry!!.value as KAmValue.Timestamp).value)

        val nullEntry = entriesByKey["null"]
        Assertions.assertInstanceOf(KAmValue.Null::class.java, nullEntry!!.value)

        val boolEntry = entriesByKey["bool"]
        Assertions.assertEquals(true, (boolEntry!!.value as KAmValue.Bool).value)
    }

    private fun assertMapEntriesAfter(r: KReadable) {
        val entries = r.mapEntries(KObjectId.Companion.ROOT)!!
        Assertions.assertEquals(1, entries.size)
        Assertions.assertEquals("newkey", entries[0].key)
        Assertions.assertEquals("newvalue", (entries[0].value as KAmValue.Str).value)
    }
}
