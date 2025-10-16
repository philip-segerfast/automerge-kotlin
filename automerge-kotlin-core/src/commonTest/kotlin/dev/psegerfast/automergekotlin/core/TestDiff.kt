package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

class TestDiff : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testTransactionAt() {
        val doc = KDocument()
        var firstHeads: KChangeHash
        var secondHeads: KChangeHash

        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            firstHeads = tx.commit()!!
        }
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 4.56
            secondHeads = tx.commit()!!
        }
        val patches = doc.diff(arrayOf(firstHeads), arrayOf(secondHeads))

        Assertions.assertEquals(patches.size, 1)
        Assertions.assertEquals(patches[0].objectId, KObjectId.Companion.ROOT)
        val action = patches[0].action as KPatchAction.PutMap
        Assertions.assertEquals(action.key, "key")
        val value = (action.value as KAmValue.F64)
        Assertions.assertEquals(value.value, 4.56)
    }
}
