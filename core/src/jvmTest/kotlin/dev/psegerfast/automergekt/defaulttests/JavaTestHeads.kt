package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.ObjectId
import org.junit.jupiter.api.Assertions

class JavaTestHeads : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testGetHeads() {
        val doc = Document()
        val tx = doc.startTransaction()
        tx[ObjectId.ROOT, "key"] = "value"
        Assertions.assertEquals(tx.heads.size, 0)
        Assertions.assertEquals(doc.heads.size, 0)
        tx.commit()
        Assertions.assertEquals(doc.heads.size, 1)
        Assertions.assertEquals(doc.heads[0].bytes.size, 32)
    }
}
