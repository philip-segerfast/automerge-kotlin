package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class TestChanges : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testApplyEncodedChanges() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx.set(KObjectId.ROOT, "key", "value")
            tx.commit()
        }
        val heads = doc.heads
        val doc2 = doc.fork()
        doc2.startTransaction().use { tx ->
            tx.set(KObjectId.ROOT, "key", "value2")
            tx.commit()
        }
        val changes = doc2.encodeChangesSince(heads)
        doc.applyEncodedChanges(changes)

        (doc[KObjectId.ROOT, "key"] as KAmValue.Str).value shouldBe "value2"
    }
}
