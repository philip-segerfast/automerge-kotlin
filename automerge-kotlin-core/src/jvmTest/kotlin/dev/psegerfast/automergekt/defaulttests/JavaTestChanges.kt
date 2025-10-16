package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ObjectId

class JavaTestChanges : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testApplyEncodedChanges() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx.set(ObjectId.ROOT, "key", "value")
            tx.commit()
        }
        val heads = doc.heads
        val doc2 = doc.fork()
        doc2.startTransaction().use { tx ->
            tx.set(ObjectId.ROOT, "key", "value2")
            tx.commit()
        }
        val changes = doc2.encodeChangesSince(heads)
        doc.applyEncodedChanges(changes)

        (doc[ObjectId.ROOT, "key"].get() as AmValue.Str).value shouldBe "value2"
    }
}
