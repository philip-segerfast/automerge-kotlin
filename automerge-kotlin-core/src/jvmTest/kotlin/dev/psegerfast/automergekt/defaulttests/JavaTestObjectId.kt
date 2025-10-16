package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.ObjectId
import org.junit.jupiter.api.Assertions

class JavaTestObjectId : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun rootObj() {
        val root = ObjectId.ROOT
        Assertions.assertTrue(root.isRoot)
    }
}
