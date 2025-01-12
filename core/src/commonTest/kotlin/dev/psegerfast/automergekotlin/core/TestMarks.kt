package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions

internal class TestMarks : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testCreateMarks() {
        val doc = KDocument()
        var text: KObjectId
        val now = Clock.System.now()
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")

            tx.mark(text, 1, 3, "bool", true, KExpandMark.BOTH)
            tx.mark(text, 1, 3, "string", "string", KExpandMark.BOTH)
            tx.mark(text, 1, 3, "bytes", "bytes".toByteArray(), KExpandMark.BOTH)
            tx.mark(text, 1, 3, "int", -1, KExpandMark.BOTH)
            tx.markUint(text, 1, 3, "uint", 1, KExpandMark.BOTH)
            tx.mark(text, 1, 3, "float", 2.5, KExpandMark.BOTH)
            tx.mark(text, 1, 3, "date", now, KExpandMark.BOTH)
//            tx.mark(text, 1, 3, "counter", Counter(5), KExpandMark.BOTH)
            tx.spliceText(text, 1, 0, "oo")
            tx.commit()
        }
        val marks = doc.marks(text)!!
        Assertions.assertEquals(8, marks.size)

        // Note that the marks are returned in alphabetical order
        val boolMark = marks[0]
        assertMark(boolMark, 1, 5, "bool") { value: KAmValue ->
            Assertions.assertTrue((value as KAmValue.Bool).value)
        }

        val bytesMark = marks[1]
        assertMark(bytesMark, 1, 5, "bytes") { value: KAmValue ->
            Assertions.assertArrayEquals(
                (value as KAmValue.Bytes).value,
                "bytes".toByteArray()
            )
        }

        val counterMark = marks[2]
        assertMark(counterMark, 1, 5, "counter") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.Counter).value,
                5
            )
        }

        val dateMark = marks[3]
        assertMark(dateMark, 1, 5, "date") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.Timestamp).value,
                now
            )
        }

        val floatMark = marks[4]
        assertMark(floatMark, 1, 5, "float") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.F64).value,
                2.5
            )
        }

        val intMark = marks[5]
        assertMark(intMark, 1, 5, "int") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.Int).value,
                -1
            )
        }

        val stringMark = marks[6]
        assertMark(stringMark, 1, 5, "string") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.Str).value,
                "string"
            )
        }

        val uintMark = marks[7]
        assertMark(uintMark, 1, 5, "uint") { value: KAmValue ->
            Assertions.assertEquals(
                (value as KAmValue.UInt).value,
                1
            )
        }
    }

    @Test
    fun testNullMark() {
        testMarkNull { doc: KDocument -> doc.startTransaction() }
    }

    @Test
    fun testMarkPatch() {
        val doc = KDocument()
        var text: KObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.mark(text, 0, 5, "bold", true, KExpandMark.BOTH)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        val action = patch.action as KPatchAction.Mark

        Assertions.assertEquals(action.marks.size, 1)
        val mark = action.marks[0]
        Assertions.assertEquals(mark.name, "bold")
        Assertions.assertEquals(mark.start, 0)
        Assertions.assertEquals(mark.end, 5)
        Assertions.assertEquals(true, (mark.value as KAmValue.Bool).value)
    }

    @Test
    fun testUnmark() {
        testUnmark { doc: KDocument -> doc.startTransaction() }
    }

    private fun testUnmark(createTx: (KDocument) -> KTransaction) {
        val doc = KDocument()
        var text: KObjectId
        createTx(doc).use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, KExpandMark.NONE)
            tx.commit()
        }
        createTx(doc).use { tx ->
            tx.unmark(text, "bold", 0, 5, KExpandMark.BOTH)
            tx.commit()
        }
        val marks = doc.marks(text)!!
        Assertions.assertEquals(marks.size, 0)
    }

    private fun testMarkNull(createTx: (KDocument) -> KTransaction) {
        val doc = KDocument()
        var text: KObjectId
        createTx(doc).use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "comment", "1234", KExpandMark.NONE)
            tx.commit()
        }
        createTx(doc).use { tx ->
            tx.markNull(text, 0, 2, "comment", KExpandMark.BOTH)
            tx.commit()
        }
        val marks = doc.marks(text)!!
        Assertions.assertEquals(1, marks.size)
        val mark = marks[0]
        assertMark(mark, 2, 5, "comment",
            MarkValueAssertion { value: KAmValue ->
                Assertions.assertEquals(
                    "1234",
                    (value as KAmValue.Str).value
                )
            })
    }

    @Test
    fun testMarksAtIndex() {
        val doc = KDocument()
        var text: KObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, KExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1)!!
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as KAmValue.Bool).value)
            tx.commit()
        }
        val marks = doc.getMarksAtIndex(text, 1)!!
        Assertions.assertEquals(1, marks.size)
        Assertions.assertEquals(true, (marks["bold"] as KAmValue.Bool).value)
    }

    @Test
    fun testMarksAtIndexAtHeads() {
        val doc = KDocument()
        var text: KObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, KExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1)!!
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as KAmValue.Bool).value)
            tx.commit()
        }
        // Save the heads
        val heads = doc.heads

        doc.startTransaction().use { tx ->
            tx.markNull(text, 0, 5, "bold", KExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1, heads)!!
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as KAmValue.Bool).value)
            tx.commit()
        }
        val marks = doc.getMarksAtIndex(text, 1, heads)!!
        Assertions.assertEquals(1, marks.size)
        Assertions.assertEquals(true, (marks["bold"] as KAmValue.Bool).value)
    }

    private fun assertMark(
        mark: KMark,
        start: Long,
        end: Long,
        name: String?,
        assertion: MarkValueAssertion
    ) {
        Assertions.assertEquals(start, mark.start)
        Assertions.assertEquals(end, mark.end)
        Assertions.assertEquals(name, mark.name)
        assertion.assertMarkValue(mark.value)
    }

    fun interface MarkValueAssertion {
        fun assertMarkValue(value: KAmValue)
    }
}
