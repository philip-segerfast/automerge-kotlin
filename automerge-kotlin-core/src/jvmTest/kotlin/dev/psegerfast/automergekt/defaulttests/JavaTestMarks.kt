package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ExpandMark
import org.automerge.Mark
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.PatchAction
import org.automerge.PatchLog
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions
import java.util.Date
import java.util.function.Function

internal class JavaTestMarks : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testCreateMarks() {
        val doc = Document()
        var text: ObjectId
        val now = Date()
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")

            tx.mark(text, 1, 3, "bool", true, ExpandMark.BOTH)
            tx.mark(text, 1, 3, "string", "string", ExpandMark.BOTH)
            tx.mark(text, 1, 3, "bytes", "bytes".toByteArray(), ExpandMark.BOTH)
            tx.mark(text, 1, 3, "int", -1, ExpandMark.BOTH)
            tx.markUint(text, 1, 3, "uint", 1, ExpandMark.BOTH)
            tx.mark(text, 1, 3, "float", 2.5, ExpandMark.BOTH)
            tx.mark(text, 1, 3, "date", now, ExpandMark.BOTH)
//            tx.mark(text, 1, 3, "counter", Counter(5), ExpandMark.BOTH)
            tx.spliceText(text, 1, 0, "oo")
            tx.commit()
        }
        val marks = doc.marks(text)
        Assertions.assertEquals(8, marks.size)

        // Note that the marks are returned in alphabetical order
        val boolMark = marks[0]
        assertMark(boolMark, 1, 5, "bool",
            MarkValueAssertion { value: AmValue -> Assertions.assertTrue((value as AmValue.Bool).value) })

        val bytesMark = marks[1]
        assertMark(bytesMark, 1, 5, "bytes",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertArrayEquals(
                    (value as AmValue.Bytes).value,
                    "bytes".toByteArray()
                )
            })

        val counterMark = marks[2]
        assertMark(counterMark, 1, 5, "counter",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.Counter).value,
                    5
                )
            })

        val dateMark = marks[3]
        assertMark(dateMark, 1, 5, "date",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.Timestamp).value,
                    now
                )
            })

        val floatMark = marks[4]
        assertMark(floatMark, 1, 5, "float",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.F64).value,
                    2.5
                )
            })

        val intMark = marks[5]
        assertMark(intMark, 1, 5, "int",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.Int).value,
                    -1
                )
            })

        val stringMark = marks[6]
        assertMark(stringMark, 1, 5, "string",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.Str).value,
                    "string"
                )
            })

        val uintMark = marks[7]
        assertMark(uintMark, 1, 5, "uint",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    (value as AmValue.UInt).value,
                    1
                )
            })
    }

    @Test
    fun testNullMark() {
        testMarkNull { doc: Document -> doc.startTransaction() }
    }

    @Test
    fun testMarkPatch() {
        val doc = Document()
        var text: ObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.commit()
        }
        val patchLog = PatchLog()
        doc.startTransaction(patchLog).use { tx ->
            tx.mark(text, 0, 5, "bold", true, ExpandMark.BOTH)
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        val action = patch.action as PatchAction.Mark

        Assertions.assertEquals(action.marks.size, 1)
        val mark = action.marks[0]
        Assertions.assertEquals(mark.name, "bold")
        Assertions.assertEquals(mark.start, 0)
        Assertions.assertEquals(mark.end, 5)
        Assertions.assertEquals(true, (mark.value as AmValue.Bool).value)
    }

    @Test
    fun testUnmark() {
        testUnmark { doc: Document -> doc.startTransaction() }
    }

    fun testUnmark(createTx: Function<Document, Transaction>) {
        val doc = Document()
        var text: ObjectId
        createTx.apply(doc).use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, ExpandMark.NONE)
            tx.commit()
        }
        createTx.apply(doc).use { tx ->
            tx.unmark(text, "bold", 0, 5, ExpandMark.BOTH)
            tx.commit()
        }
        val marks = doc.marks(text)
        Assertions.assertEquals(marks.size, 0)
    }

    fun testMarkNull(createTx: Function<Document, Transaction>) {
        val doc = Document()
        var text: ObjectId
        createTx.apply(doc).use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "comment", "1234", ExpandMark.NONE)
            tx.commit()
        }
        createTx.apply(doc).use { tx ->
            tx.markNull(text, 0, 2, "comment", ExpandMark.BOTH)
            tx.commit()
        }
        val marks = doc.marks(text)
        Assertions.assertEquals(1, marks.size)
        val mark = marks[0]
        assertMark(mark, 2, 5, "comment",
            MarkValueAssertion { value: AmValue ->
                Assertions.assertEquals(
                    "1234",
                    (value as AmValue.Str).value
                )
            })
    }

    @Test
    fun testMarksAtIndex() {
        val doc = Document()
        var text: ObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, ExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1)
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as AmValue.Bool).value)
            tx.commit()
        }
        val marks = doc.getMarksAtIndex(text, 1)
        Assertions.assertEquals(1, marks.size)
        Assertions.assertEquals(true, (marks["bold"] as AmValue.Bool).value)
    }

    @Test
    fun testMarksAtIndexAtHeads() {
        val doc = Document()
        var text: ObjectId
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "Hello")
            tx.mark(text, 0, 5, "bold", true, ExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1)
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as AmValue.Bool).value)
            tx.commit()
        }
        // Save the heads
        val heads = doc.heads

        doc.startTransaction().use { tx ->
            tx.markNull(text, 0, 5, "bold", ExpandMark.NONE)
            val marks = tx.getMarksAtIndex(text, 1, heads)
            Assertions.assertEquals(1, marks.size)
            Assertions.assertEquals(true, (marks["bold"] as AmValue.Bool).value)
            tx.commit()
        }
        val marks = doc.getMarksAtIndex(text, 1, heads)
        Assertions.assertEquals(1, marks.size)
        Assertions.assertEquals(true, (marks["bold"] as AmValue.Bool).value)
    }

    fun assertMark(
        mark: Mark,
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
        fun assertMarkValue(value: AmValue)
    }
}
