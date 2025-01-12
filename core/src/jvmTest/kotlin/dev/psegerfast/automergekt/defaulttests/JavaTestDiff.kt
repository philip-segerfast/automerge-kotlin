package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.ChangeHash
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.PatchAction.PutMap
import org.junit.jupiter.api.Assertions

class JavaTestDiff : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testTransactionAt() {
        val doc = Document()
        var firstHeads: ChangeHash
        var secondHeads: ChangeHash

        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            firstHeads = tx.commit().get()
        }
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 4.56
            secondHeads = tx.commit().get()
        }
        val patches = doc.diff(arrayOf(firstHeads), arrayOf(secondHeads))

        Assertions.assertEquals(patches.size, 1)
        Assertions.assertEquals(patches[0].obj, ObjectId.ROOT)
        val action = patches[0].action as PutMap
        Assertions.assertEquals(action.key, "key")
        val value = (action.value as AmValue.F64)
        Assertions.assertEquals(value.value, 4.56)
    }
}
