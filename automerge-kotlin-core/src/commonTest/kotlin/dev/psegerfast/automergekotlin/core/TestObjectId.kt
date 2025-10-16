package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

class TestObjectId : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun rootObj() {
        val root = KObjectId.Companion.ROOT
        Assertions.assertTrue(root.isRoot)
    }
}
