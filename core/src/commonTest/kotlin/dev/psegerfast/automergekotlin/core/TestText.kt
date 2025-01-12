package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TestText : FunSpec({
    lateinit var textId: KObjectId
    lateinit var doc: KDocument

    // Before each test (leaf case)
    beforeEach {
        println("beforeEach: ${it.name.testName}")

        doc = KDocument().also {
            it.startTransaction().also { tx ->
                textId = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            }.commit()
        }
    }

    test("Test spliceText") {
        val tx = doc.startTransaction()
        tx.spliceText(textId, 0, 0, "hello")

        tx.text(textId) shouldBe "hello"
        doc.text(textId) shouldBe "hello"
    }

    test("Test get non-text in document") {
        val otherDoc = KDocument()
        val otherTx = otherDoc.startTransaction()
        val map = otherTx.set(KObjectId.Companion.ROOT, "map", KObjectType.MAP)
        otherTx.commit()
        otherDoc.text(map) shouldBe null
    }

    test("Test get non-text in transaction") {
        val otherDoc = KDocument()
        val otherTx = otherDoc.startTransaction()
        val map = otherTx.set(KObjectId.Companion.ROOT, "map", KObjectType.MAP)
        otherTx.text(map) shouldBe null
    }

    test("testTextAt") {
        doc.startTransaction().also { tx ->
            tx.spliceText(textId, 0, 0, "hello")
        }.commit()

        val heads = doc.heads
        doc.startTransaction().also { tx ->
            tx.spliceText(textId, 5, 0, " world")
            tx.text(textId) shouldBe "hello world"
            tx.text(textId, heads) shouldBe "hello"
            doc.text(textId, heads) shouldBe "hello"
        }.commit()

        doc.text(textId, heads) shouldBe "hello"
    }

}) {
    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }
}
