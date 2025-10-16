package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Patch
import org.automerge.PatchAction
import org.automerge.PatchAction.DeleteList
import org.automerge.PatchAction.DeleteMap
import org.automerge.PatchAction.PutList
import org.automerge.PatchAction.PutMap
import org.automerge.PatchAction.SpliceText
import org.automerge.PatchLog
import org.automerge.PathElement
import org.junit.jupiter.api.Assertions
import java.util.Date

internal class JavaTestPatches : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testSetInMap() {
        val doc = Document()
        val patchlog = PatchLog()
        val tx = doc.startTransaction(patchlog)
        tx[ObjectId.ROOT, "uint"] = NewValue.uint(0)
        tx[ObjectId.ROOT, "int"] = 1
        tx[ObjectId.ROOT, "float"] = 2.0
        tx[ObjectId.ROOT, "string"] = "string"
        tx[ObjectId.ROOT, "bytes"] = "bytes".toByteArray()
        tx[ObjectId.ROOT, "bool"] = true
        tx[ObjectId.ROOT, "null"] = NewValue.NULL
//        tx[ObjectId.ROOT, "counter"] = Counter(3)
        val now = Date()
        tx[ObjectId.ROOT, "timestamp"] = now
        val list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
        val map = tx.set(ObjectId.ROOT, "map", ObjectType.MAP)
        val text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchlog)

        assertPutMap(
            patches[0], ObjectId.ROOT, emptyPath(), "uint"
        ) { value: AmValue ->
            Assertions.assertEquals(
                (value as AmValue.UInt).value,
                0
            )
        }

        assertPutMap(
            patches[1], ObjectId.ROOT, emptyPath(), "int",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Int).value, 1)
            })

        assertPutMap(
            patches[2], ObjectId.ROOT, emptyPath(), "float",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.F64).value, 2.0)
            })

        assertPutMap(
            patches[3], ObjectId.ROOT, emptyPath(), "string",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Str).value, "string")
            })

        assertPutMap(
            patches[4], ObjectId.ROOT, emptyPath(), "bytes",
            ValueAssertion { value: AmValue ->
                Assertions.assertArrayEquals((value as AmValue.Bytes).value, "bytes".toByteArray())
            })

        assertPutMap(
            patches[5], ObjectId.ROOT, emptyPath(), "bool",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Bool).value, true)
            })

        assertPutMap(
            patches[6], ObjectId.ROOT, emptyPath(), "null",
            ValueAssertion { value: AmValue? ->
                Assertions.assertInstanceOf(
                    AmValue.Null::class.java, value
                )
            })

        assertPutMap(
            patches[7], ObjectId.ROOT, emptyPath(), "counter",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Counter).value, 3)
            })

        assertPutMap(
            patches[8], ObjectId.ROOT, emptyPath(), "timestamp",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Timestamp).value, now)
            })

        assertPutMap(
            patches[9], ObjectId.ROOT, emptyPath(), "list",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.List).id, list)
            })

        assertPutMap(
            patches[10], ObjectId.ROOT, emptyPath(), "map",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Map).id, map)
            })

        assertPutMap(
            patches[11], ObjectId.ROOT, emptyPath(), "text",
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Text).id, text)
            })
        patchlog.free()
    }

    @Test
    fun testSetInList() {
        val doc = Document()
        var list: ObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
            for (i in 0..11) {
                tx.insert(list, i.toLong(), NewValue.NULL)
            }
            tx.commit()
        }
        val patchLog = PatchLog()
        val tx = doc.startTransaction(patchLog)
        tx[list, 0] = NewValue.uint(0)

        tx[list, 1] = 1
        tx[list, 2] = 2.0
        tx[list, 3] = "string"
        tx[list, 4] = "bytes".toByteArray()
        tx[list, 5] = true

        // Have to set index 6 to non-null otherwise the setNull is a noop and so no
        // observer method is called
        tx[list, 6] = 4
        tx[list, 6] = NewValue.NULL

//        tx[list, 7] = Counter(3)
        val now = Date()
        tx[list, 8] = now
        val innerList = tx.set(list, 9, ObjectType.LIST)
        val map = tx.set(list, 10, ObjectType.MAP)
        val text = tx.set(list, 11, ObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchLog)

        assertSetInList(
            patches[0], list, KPathBuilder.root("list").build(), 0,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.UInt).value, 0)
            })

        assertSetInList(
            patches[1], list, KPathBuilder.root("list").build(), 1,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Int).value, 1)
            })

        assertSetInList(
            patches[2], list, KPathBuilder.root("list").build(), 2,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.F64).value, 2.0)
            })

        assertSetInList(
            patches[3], list, KPathBuilder.root("list").build(), 3,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Str).value, "string")
            })

        assertSetInList(
            patches[4], list, KPathBuilder.root("list").build(), 4,
            ValueAssertion { value: AmValue ->
                Assertions.assertArrayEquals((value as AmValue.Bytes).value, "bytes".toByteArray())
            })

        assertSetInList(
            patches[5], list, KPathBuilder.root("list").build(), 5,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Bool).value, true)
            })

        // Note we skip an index here due to the patch which sets the 6th index to
        // non-null
        assertSetInList(
            patches[7], list, KPathBuilder.root("list").build(), 6,
            ValueAssertion { value: AmValue? ->
                Assertions.assertInstanceOf<AmValue.Null>(
                    AmValue.Null::class.java, value
                )
            })

        assertSetInList(
            patches[8], list, KPathBuilder.root("list").build(), 7,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Counter).value, 3)
            })

        assertSetInList(
            patches[9], list, KPathBuilder.root("list").build(), 8,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Timestamp).value, now)
            })

        assertSetInList(
            patches[10], list, KPathBuilder.root("list").build(), 9,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.List).id, innerList)
            })

        assertSetInList(
            patches[11], list, KPathBuilder.root("list").build(), 10,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Map).id, map)
            })

        assertSetInList(
            patches[12], list, KPathBuilder.root("list").build(), 11,
            ValueAssertion { value: AmValue ->
                Assertions.assertEquals((value as AmValue.Text).id, text)
            })
    }

    @Test
    fun testInsertInList() {
        val doc = Document()
        var list: ObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
            tx.commit()
        }
        val patchLog = PatchLog()
        val tx = doc.startTransaction(patchLog)

        tx.insert(list, 0, NewValue.uint(0))
        tx.insert(list, 1, 1)
        tx.insert(list, 2, 2.0)
        tx.insert(list, 3, "string")
        tx.insert(list, 4, "bytes".toByteArray())
        tx.insert(list, 5, true)
        tx.insert(list, 6, NewValue.NULL)
//        tx.insert(list, 7, Counter(3))
        val now = Date()
        tx.insert(list, 8, now)
        val innerList = tx.insert(list, 9, ObjectType.LIST)
        val map = tx.insert(list, 10, ObjectType.MAP)
        val text = tx.insert(list, 11, ObjectType.TEXT)

        tx.commit()
        val patches = doc.makePatches(patchLog)
        Assertions.assertEquals(patches.size, 1)

        val patch = patches[0]
        Assertions.assertEquals(patch.obj, list)
        // Assertions.assertEquals(patch.getPath(), path(new PathElement(ObjectId.ROOT,
        // new
        // Prop("list")));
        Assertions.assertEquals(patch.path, KPathBuilder.root("list").build())
        val values = (patch.action as PatchAction.Insert).values

        Assertions.assertEquals((values[0] as AmValue.UInt).value, 0)
        Assertions.assertEquals((values[1] as AmValue.Int).value, 1)
        Assertions.assertEquals((values[2] as AmValue.F64).value, 2.0)
        Assertions.assertEquals((values[3] as AmValue.Str).value, "string")
        Assertions.assertArrayEquals((values[4] as AmValue.Bytes).value, "bytes".toByteArray())
        Assertions.assertEquals((values[5] as AmValue.Bool).value, true)
        Assertions.assertInstanceOf(
            AmValue.Null::class.java,
            values[6]
        )
        Assertions.assertEquals((values[7] as AmValue.Counter).value, 3)
        Assertions.assertEquals((values[8] as AmValue.Timestamp).value, now)
        Assertions.assertEquals((values[9] as AmValue.List).id, innerList)
        Assertions.assertEquals((values[10] as AmValue.Map).id, map)
        Assertions.assertEquals((values[11] as AmValue.Text).id, text)
    }

    @Test
    fun testSpliceText() {
        val doc = Document()
        var text: ObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.commit()
        }
        val patchLog = PatchLog()
        val tx = doc.startTransaction(patchLog)
        tx.spliceText(text, 5, 0, " world")

        tx.commit()
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.obj, text)
        Assertions.assertEquals(patch.path.map { it.toMyPathElement() }, KPathBuilder.root("text").build())

        val action = patch.action as SpliceText
        Assertions.assertEquals(action.index, 5)
        Assertions.assertEquals(action.text, " world")
    }

    @Test
    fun testFlagConflictMap() {
        val doc1 = Document("bbbb".toByteArray())
        val doc2 = Document("aaaa".toByteArray())
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value_1"
            tx.commit()
        }
        doc2.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value_2"
            tx.commit()
        }
        val patchLog = PatchLog()
        doc2.merge(doc1, patchLog)
        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.obj, ObjectId.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())

        val action = patch.action as PutMap
        Assertions.assertTrue(action.isConflict)
    }

    @Test
    fun testFlagConflictList() {
        val doc1 = Document("bbbb".toByteArray())
        var list: ObjectId
        doc1.startTransaction().use { tx ->
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
            tx.insert(list, 0, NewValue.NULL)
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
        val patchLog = PatchLog()
        doc2.merge(doc1, patchLog)
        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.obj, list)
        Assertions.assertEquals(patch.path.map { it.toMyPathElement() }, KPathBuilder.root("list").build())

        val action = patch.action as PutList
        Assertions.assertTrue(action.isConflict)
    }

//    @Test
//    fun testIncrementInMap() {
//        val doc = Document()
//        doc.startTransaction().use { tx ->
//            tx[ObjectId.ROOT, "counter"] = Counter(0)
//            tx.commit()
//        }
//        val patchLog = PatchLog()
//        doc.startTransaction(patchLog).use { tx ->
//            tx.increment(ObjectId.ROOT, "counter", 5)
//            tx.commit()
//        }
//        val patches = doc.makePatches(patchLog)
//
//        Assertions.assertEquals(patches.size, 1)
//        val patch = patches[0]
//
//        Assertions.assertEquals(patch.obj, ObjectId.ROOT)
//        Assertions.assertEquals(patch.path, emptyPath())
//        Assertions.assertEquals((patch.action as PatchAction.Increment).value, 5)
//    }

    @Test
    fun testIncrementInList() {
        val doc = Document()
        var list: ObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
//            tx.insert(list, 0, Counter(10))
            tx.commit()
        }
        val patchLog = PatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.increment(list, 0, 5)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.obj, list)
        Assertions.assertEquals(patch.path, KPathBuilder.root("list").build())
        Assertions.assertEquals((patch.action as PatchAction.Increment).value, 5)
    }

    @Test
    fun testDeleteInMap() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val patchLog = PatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.delete(ObjectId.ROOT, "key")
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.obj, ObjectId.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())
        val action = patch.action as DeleteMap
        Assertions.assertEquals(action.key, "key")
    }

    @Test
    fun testDeleteInList() {
        val doc = Document()
        var list: ObjectId
        doc.startTransaction().use { tx ->
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
            tx.insert(list, 0, "value")
            tx.insert(list, 1, "value2")
            tx.commit()
        }
        val patchLog = PatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.delete(list, 0)
            tx.delete(list, 0)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch1 = patches[0]
        Assertions.assertEquals(patch1.obj, list)
        Assertions.assertEquals(patch1.path.map { it.toMyPathElement() }, KPathBuilder.root("list").build())
        val action1 = patch1.action as DeleteList
        Assertions.assertEquals(action1.index, 0)
        Assertions.assertEquals(action1.length, 2)
    }

    @Test
    fun testApplyEncodedChangesForPatches() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val changes = doc.encodeChangesSince(arrayOf())
        val doc2 = Document()
        val patchLog = PatchLog()
        doc2.applyEncodedChanges(changes, patchLog)

        val patches = doc2.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]

        Assertions.assertEquals(patch.obj, ObjectId.ROOT)
        Assertions.assertEquals(patch.path, emptyPath())

        val action = patch.action as PutMap
        Assertions.assertEquals(action.key, "key")
    }

    fun emptyPath(): ArrayList<PathElement> {
        return KPathBuilder.empty()
    }

    fun assertPutMap(
        patch: Patch,
        expectedObj: ObjectId?,
        expectedPath: ArrayList<PathElement>,
        expectedKey: String?,
        value: ValueAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.obj, expectedObj)
        Assertions.assertEquals((patch.action as PutMap).key, expectedKey)
        value.assertValue((patch.action as PutMap).value)
    }

    fun assertSetInList(
        patch: Patch,
        expectedObj: ObjectId,
        expectedPath: ArrayList<PathElement>,
        expectedIndex: Long,
        value: ValueAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.obj, expectedObj)
        Assertions.assertEquals((patch.action as PutList).index, expectedIndex)
        value.assertValue((patch.action as PutList).value)
    }

    fun assertInsertInList(
        patch: Patch,
        expectedObj: ObjectId?,
        expectedPath: ArrayList<PathElement?>?,
        expectedIndex: Long,
        value: ValuesAssertion
    ) {
        Assertions.assertEquals(patch.path, expectedPath)
        Assertions.assertEquals(patch.obj, expectedObj)
        Assertions.assertEquals((patch.action as PatchAction.Insert).index, expectedIndex)
        value.assertValues((patch.action as PatchAction.Insert).values)
    }

    fun interface ValueAssertion {
        fun assertValue(value: AmValue)
    }

    fun interface ValuesAssertion {
        fun assertValues(values: ArrayList<AmValue?>)
    }
}
