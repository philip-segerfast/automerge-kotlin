package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

class TestTransactionAt : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testTransactionAt() {
        val doc = KDocument()
        var firstHeads: KChangeHash

        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            firstHeads = tx.commit()!!
        }
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 4.56
            tx.commit()
        }
        val patchLog = KPatchLog()
        doc.startTransactionAt(patchLog, arrayOf(firstHeads)).use { tx ->
            val result = tx[KObjectId.Companion.ROOT, "key"]
            Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
            tx[KObjectId.Companion.ROOT, "key"] = 7.89
            tx.commit()
        }
        val patches = doc.makePatches(patchLog)

        Assertions.assertEquals(patches.size, 1)
        Assertions.assertEquals(patches[0].objectId, KObjectId.Companion.ROOT)
        val action = patches[0].action as KPatchAction.PutMap
        Assertions.assertEquals(action.key, "key")
        val value = (action.value as KAmValue.F64)
        Assertions.assertEquals(value.value, 7.89)
    }
}
