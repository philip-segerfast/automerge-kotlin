package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions

internal class TestPatches : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testSetInMap() {
        val doc = KDocument()
        val patchlog = KPatchLog()
        val tx = doc.startTransaction(patchlog)
        tx[KObjectId.Companion.ROOT, "uint"] = KNewValue.Companion.uint(0)
        tx[KObjectId.Companion.ROOT, "int"] = 1
        tx[KObjectId.Companion.ROOT, "float"] = 2.0
        tx[KObjectId.Companion.ROOT, "string"] = "string"
        tx[KObjectId.Companion.ROOT, "bytes"] = "bytes".toByteArray()
        tx[KObjectId.Companion.ROOT, "bool"] = true
        tx[KObjectId.Companion.ROOT, "null"] = KNewValue.Companion.Null
//        tx[KObjectId.ROOT, "counter"] = Counter(3)
        val now = Clock.System.now()
        tx[KObjectId.Companion.ROOT, "timestamp"] = now
        val list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
        val map = tx.set(KObjectId.Companion.ROOT, "map", KObjectType.MAP)
        val text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchlog)

        assertPutMap(
            patches[0], KObjectId.Companion.ROOT, emptyPath(), "uint"
        ) { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.UInt).value,
                0
            )
        }

        assertPutMap(patches[1], KObjectId.Companion.ROOT, emptyPath(), "int") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Int).value, 1)
        }

        assertPutMap(patches[2], KObjectId.Companion.ROOT, emptyPath(), "float") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.F64).value, 2.0)
        }

        assertPutMap(patches[3], KObjectId.Companion.ROOT, emptyPath(), "string") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Str).value, "string")
        }

        assertPutMap(patches[4], KObjectId.Companion.ROOT, emptyPath(), "bytes") { value: KAmValue ->
            Assertions.assertArrayEquals((value as KAmValue.Bytes).value, "bytes".toByteArray())
        }

        assertPutMap(patches[5], KObjectId.Companion.ROOT, emptyPath(), "bool") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Bool).value, true)
        }

        assertPutMap(patches[6], KObjectId.Companion.ROOT, emptyPath(), "null") { value: KAmValue? ->
            Assertions.assertInstanceOf(
                KAmValue.Null::class.java, value
            )
        }

        assertPutMap(patches[7], KObjectId.Companion.ROOT, emptyPath(), "counter") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Counter).value, 3)
        }

        assertPutMap(patches[8], KObjectId.Companion.ROOT, emptyPath(), "timestamp") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Timestamp).value, now)
        }

        assertPutMap(patches[9], KObjectId.Companion.ROOT, emptyPath(), "list") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.List).id, list)
        }

        assertPutMap(patches[10], KObjectId.Companion.ROOT, emptyPath(), "map") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Map).id, map)
        }

        assertPutMap(patches[11], KObjectId.Companion.ROOT, emptyPath(), "text") { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Text).id, text)
        }

        patchlog.free()
    }

    @Test
    fun testSetInList() {
        val doc = KDocument()
        var list: KObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
            for (i in 0..11) {
                tx.insert(list, i.toLong(), KNewValue.Companion.Null)
            }
            tx.commit()
        }
        val patchLog = KPatchLog()
        val tx = doc.startTransaction(patchLog)
        tx[list, 0] = KNewValue.Companion.uint(0)

        tx[list, 1] = 1
        tx[list, 2] = 2.0
        tx[list, 3] = "string"
        tx[list, 4] = "bytes".toByteArray()
        tx[list, 5] = true

        // Have to set index 6 to non-null otherwise the setNull is a noop and so no
        // observer method is called
        tx[list, 6] = 4
        tx[list, 6] = KNewValue.Companion.Null

//        tx[list, 7] = Counter(3)
        val now = Clock.System.now()
        tx[list, 8] = now
        val innerList = tx.set(list, 9, KObjectType.LIST)
        val map = tx.set(list, 10, KObjectType.MAP)
        val text = tx.set(list, 11, KObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchLog)

        assertSetInList(patches[0], list, KPathBuilder.root("list").build(), 0) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.UInt).value, 0)
        }

        assertSetInList(patches[1], list, KPathBuilder.root("list").build(), 1) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Int).value, 1)
        }

        assertSetInList(patches[2], list, KPathBuilder.root("list").build(), 2) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.F64).value, 2.0)
        }

        assertSetInList(patches[3], list, KPathBuilder.root("list").build(), 3) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Str).value, "string")
        }

        assertSetInList(patches[4], list, KPathBuilder.root("list").build(), 4) { value: KAmValue ->
            Assertions.assertArrayEquals((value as KAmValue.Bytes).value, "bytes".toByteArray())
        }

        assertSetInList(patches[5], list, KPathBuilder.root("list").build(), 5) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Bool).value, true)
        }

        // Note we skip an index here due to the patch which sets the 6th index to
        // non-null
        assertSetInList(
            patches[7],
            list,
            KPathBuilder.root("list").build(),
            6
        ) { value: KAmValue? ->
            Assertions.assertInstanceOf<KAmValue.Null>(
                KAmValue.Null::class.java, value
            )
        }

        assertSetInList(patches[8], list, KPathBuilder.root("list").build(), 7) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Counter).value, 3)
        }

        assertSetInList(patches[9], list, KPathBuilder.root("list").build(), 8) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Timestamp).value, now)
        }

        assertSetInList(
            patches[10],
            list,
            KPathBuilder.root("list").build(),
            9
        ) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.List).id, innerList)
        }

        assertSetInList(
            patches[11],
            list,
            KPathBuilder.root("list").build(),
            10
        ) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Map).id, map)
        }

        assertSetInList(
            patches[12],
            list,
            KPathBuilder.root("list").build(),
            11
        ) { value: KAmValue ->
            Assertions.assertEquals((value as KAmValue.Text).id, text)
        }
    }

    @Test
    fun testInsertInList() {
        val doc = KDocument()
        var list: KObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
            tx.commit()
        }
        val patchLog = KPatchLog()
        val tx = doc.startTransaction(patchLog)

        tx.insert(list, 0, KNewValue.Companion.uint(0))
        tx.insert(list, 1, 1)
        tx.insert(list, 2, 2.0)
        tx.insert(list, 3, "string")
        tx.insert(list, 4, "bytes".toByteArray())
        tx.insert(list, 5, true)
        tx.insert(list, 6, KNewValue.Companion.Null)
//        tx.insert(list, 7, Counter(3))
        val now = Clock.System.now()
        tx.insert(list, 8, now)
        val innerList = tx.insert(list, 9, KObjectType.LIST)
        val map = tx.insert(list, 10, KObjectType.MAP)
        val text = tx.insert(list, 11, KObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchLog)
        Assertions.assertEquals(patches.size, 1)

        val patch = patches[0]
        Assertions.assertEquals(patch.objectId, list)
        // Assertions.assertEquals(patch.getPath(), path(new KPathElement(KObjectId.ROOT,
        // new
        // Prop("list")));
        Assertions.assertEquals(patch.path, KPathBuilder.root("list").build())
        val values = (patch.action as KPatchAction.Insert).values

        Assertions.assertEquals((values[0] as KAmValue.UInt).value, 0)
        Assertions.assertEquals((values[1] as KAmValue.Int).value, 1)
        Assertions.assertEquals((values[2] as KAmValue.F64).value, 2.0)
        Assertions.assertEquals((values[3] as KAmValue.Str).value, "string")
        Assertions.assertArrayEquals((values[4] as KAmValue.Bytes).value, "bytes".toByteArray())
        Assertions.assertEquals((values[5] as KAmValue.Bool).value, true)
        Assertions.assertInstanceOf(
            KAmValue.Null::class.java,
            values[6]
        )
        Assertions.assertEquals((values[7] as KAmValue.Counter).value, 3)
        Assertions.assertEquals((values[8] as KAmValue.Timestamp).value, now)
        Assertions.assertEquals((values[9] as KAmValue.List).id, innerList)
        Assertions.assertEquals((values[10] as KAmValue.Map).id, map)
        Assertions.assertEquals((values[11] as KAmValue.Text).id, text)
    }

    @Test
    fun testSpliceText() {
        val doc = KDocument()
        var text: KObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.commit()
        }
        val patchLog = KPatchLog()
        val tx = doc.startTransaction(patchLog)
        tx.spliceText(text, 5, 0, " world")

        tx.commit()
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.objectId, text)
        Assertions.assertEquals(patch.path, KPathBuilder.root("text").build())

        val action = patch.action as KPatchAction.SpliceText
        Assertions.assertEquals(action.index, 5)
        Assertions.assertEquals(action.text, " world")
    }

    @Test
    fun testFlagConflictMap() {
        val doc1 = KDocument("bbbb".toByteArray())
        val doc2 = KDocument("aaaa".toByteArray())
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value_1"
            tx.commit()
        }
        doc2.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value_2"
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc2.merge(doc1, patchLog)
        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.objectId, KObjectId.Companion.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())

        val action = patch.action as KPatchAction.PutMap
        Assertions.assertTrue(action.isConflict)
    }

    @Test
    fun testFlagConflictList() {
        val doc1 = KDocument("bbbb".toByteArray())
        var list: KObjectId
        doc1.startTransaction().use { tx ->
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
            tx.insert(list, 0, KNewValue.Companion.Null)
            tx.commit()
        }
        val doc2 = doc1.fork("aaaa".toByteArray())
        doc2.startTransaction().use { tx ->
            tx[list, 0] = "value_2"
            tx.commit()
        }
        doc1.startTransaction().use { tx ->
            tx[list, 0] = "value_1"
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc2.merge(doc1, patchLog)
        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.objectId, list)
        Assertions.assertEquals(patch.path, KPathBuilder.root("list").build())

        val action = patch.action as KPatchAction.PutList
        Assertions.assertTrue(action.isConflict)
    }

    @Test
    fun testIncrementInMap() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
//            tx[KObjectId.ROOT, "counter"] = Counter(0)
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.increment(KObjectId.Companion.ROOT, "counter", 5)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.objectId, KObjectId.Companion.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())
        Assertions.assertEquals((patch.action as KPatchAction.Increment).value, 5)
    }

    @Test
    fun testIncrementInList() {
        val doc = KDocument()
        var list: KObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
//            tx.insert(list, 0, Counter(10))
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.increment(list, 0, 5)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.objectId, list)
        Assertions.assertEquals(patch.path, KPathBuilder.root("list").build())
        Assertions.assertEquals((patch.action as KPatchAction.Increment).value, 5)
    }

    @Test
    fun testDeleteInMap() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.delete(KObjectId.Companion.ROOT, "key")
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.objectId, KObjectId.Companion.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())
        val action = patch.action as KPatchAction.DeleteMap
        Assertions.assertEquals(action.key, "key")
    }

    @Test
    fun testDeleteInList() {
        val doc = KDocument()
        var list: KObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
            tx.insert(list, 0, "value")
            tx.insert(list, 1, "value2")
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.delete(list, 0)
            tx.delete(list, 0)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch1 = patches[0]
        Assertions.assertEquals(patch1.objectId, list)
        Assertions.assertEquals(patch1.path, KPathBuilder.root("list").build())
        val action1 = patch1.action as KPatchAction.DeleteList
        Assertions.assertEquals(action1.index, 0)
        Assertions.assertEquals(action1.length, 2)
    }

    @Test
    fun testApplyEncodedChangesForPatches() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val changes = doc.encodeChangesSince(arrayOf())
        val doc2 = KDocument()
        val patchLog = KPatchLog()
        doc2.applyEncodedChanges(changes, patchLog)

        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.objectId, KObjectId.Companion.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())

        val action = patch.action as KPatchAction.PutMap
        Assertions.assertEquals(action.key, "key")
    }

    private fun emptyPath(): ArrayList<KPathElement> {
        return KPathBuilder.empty()
    }

    private fun assertPutMap(
        patch: KPatch,
        expectedObj: KObjectId?,
        expectedPath: ArrayList<KPathElement>,
        expectedKey: String?,
        value: ValueAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.objectId, expectedObj)
        Assertions.assertEquals((patch.action as KPatchAction.PutMap).key, expectedKey)
        value.assertValue((patch.action as KPatchAction.PutMap).value)
    }

    private fun assertSetInList(
        patch: KPatch,
        expectedObj: KObjectId,
        expectedPath: ArrayList<KPathElement>,
        expectedIndex: Long,
        value: ValueAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.objectId, expectedObj)
        Assertions.assertEquals((patch.action as KPatchAction.PutList).index, expectedIndex)
        value.assertValue((patch.action as KPatchAction.PutList).value)
    }

    fun assertInsertInList(
        patch: KPatch,
        expectedObj: KObjectId?,
        expectedPath: ArrayList<KPathElement?>?,
        expectedIndex: Long,
        value: ValuesAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.objectId, expectedObj)
        Assertions.assertEquals((patch.action as KPatchAction.Insert).index, expectedIndex)
        value.assertValues((patch.action as KPatchAction.Insert).values)
    }

    fun interface ValueAssertion {
        fun assertValue(value: KAmValue)
    }

    fun interface ValuesAssertion {
        fun assertValues(values: List<KAmValue>)
    }
}
