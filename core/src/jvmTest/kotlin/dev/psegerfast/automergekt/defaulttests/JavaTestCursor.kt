package dev.psegerfast.automergekt.defaulttests

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.automerge.Cursor
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType

class JavaTestCursor : AnnotationSpec() {
    private lateinit var doc: Document
    private lateinit var text: ObjectId

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        doc.startTransaction().use { tx ->
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.spliceText(text, 0, 0, "hello world")
            tx.commit()
        }
    }

    @Test
    fun testCursorInDoc() {
        val cursor = doc.makeCursor(text, 3)
        doc.lookupCursorIndex(text, cursor) shouldBe 3

        val heads = doc.heads

        doc.startTransaction().use { tx ->
            tx.spliceText(text, 3, 0, "!")
            tx.commit()
        }
        doc.lookupCursorIndex(text, cursor) shouldBe 4
        doc.lookupCursorIndex(text, cursor, heads) shouldBe 3

        val oldCursor = doc.makeCursor(text, 3, heads)
        doc.lookupCursorIndex(text, oldCursor) shouldBe 4
        doc.lookupCursorIndex(text, oldCursor, heads) shouldBe 3
    }

    @Test
    fun testCursorInTx() {
        val heads = doc.heads
        var cursor: Cursor
        doc.startTransaction().use { tx ->
            cursor = tx.makeCursor(text, 3)
            tx.lookupCursorIndex(text, cursor) shouldBe 3
            tx.spliceText(text, 3, 0, "!")
            tx.lookupCursorIndex(text, cursor) shouldBe 4
            tx.commit()
        }
        doc.startTransaction().use { tx ->
            val oldCursor = tx.makeCursor(text, 3, heads)
            tx.lookupCursorIndex(text, oldCursor) shouldBe 4
            tx.lookupCursorIndex(text, oldCursor, heads) shouldBe 3
            tx.commit()
        }
    }

    @Test
    fun testToFromString() {
        val cursor = doc.makeCursor(text, 3)
        val encoded = cursor.toString()
        val decoded = Cursor.fromString(encoded)
        doc.lookupCursorIndex(text, decoded) shouldBe 3

        shouldThrow<IllegalArgumentException> {
            Cursor.fromString("invalid")
        }
    }

    @Test
    fun testToFromBytes() {
        val cursor = doc.makeCursor(text, 3)
        val encoded = cursor.toBytes()
        val decoded = Cursor.fromBytes(encoded)
        doc.lookupCursorIndex(text, decoded) shouldBe 3

        shouldThrow<IllegalArgumentException> {
            Cursor.fromBytes(byteArrayOf(0x01, 0x01))
        }
    }
}
