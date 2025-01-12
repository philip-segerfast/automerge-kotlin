package dev.psegerfast.automergekotlin.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class TestCursor : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var text: KObjectId

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
        doc.startTransaction().use { tx ->
            text = tx.set(KObjectId.ROOT, "text", KObjectType.TEXT)
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
        var cursor: KCursor
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
        val decoded = KCursor.fromString(encoded)
        doc.lookupCursorIndex(text, decoded) shouldBe 3

        shouldThrow<IllegalArgumentException> {
            KCursor.fromString("invalid")
        }
    }

    @Test
    fun testToFromBytes() {
        val cursor = doc.makeCursor(text, 3)
        val encoded = cursor.toBytes()
        val decoded = KCursor.fromBytes(encoded)
        doc.lookupCursorIndex(text, decoded) shouldBe 3

        shouldThrow<IllegalArgumentException> {
            KCursor.fromBytes(byteArrayOf(0x01, 0x01))
        }
    }
}
