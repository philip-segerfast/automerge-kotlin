package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.ChangeHash
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.PatchAction.PutMap
import org.automerge.PatchLog
import org.junit.jupiter.api.Assertions

class JavaTestTransactionAt : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testTransactionAt() {
        val doc = Document()
        var firstHeads: ChangeHash

        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            firstHeads = tx.commit().get()
        }
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 4.56
            tx.commit().get()
        }
        val patchLog = PatchLog()
        doc.startTransactionAt(patchLog, arrayOf(firstHeads)).use { tx ->
            val resulut = tx[ObjectId.ROOT, "key"]
            Assertions.assertEquals(1.23, (resulut.get() as AmValue.F64).value)
            tx[ObjectId.ROOT, "key"] = 7.89
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        Assertions.assertEquals(patches[0].obj, ObjectId.ROOT)
        val action = patches[0].action as PutMap
        Assertions.assertEquals(action.key, "key")
        val value = (action.value as AmValue.F64)
        Assertions.assertEquals(value.value, 7.89)
    }
}
