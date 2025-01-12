package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

class TestHeads : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testGetHeads() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        tx[KObjectId.Companion.ROOT, "key"] = "value"
        Assertions.assertEquals(tx.heads.size, 0)
        Assertions.assertEquals(doc.heads.size, 0)
        tx.commit()
        Assertions.assertEquals(doc.heads.size, 1)
        Assertions.assertEquals(doc.heads[0].bytes.size, 32)
    }
}
