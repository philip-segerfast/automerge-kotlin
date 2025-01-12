package dev.psegerfast.automergekt.defaulttests

import dev.psegerfast.automergekotlin.core.TEST_METHOD
import dev.psegerfast.automergekotlin.core.TestMethod
import io.kotest.core.spec.RootTest

val RUN_AUTOMERGE_JAVA_TESTS = when(TEST_METHOD) {
    TestMethod.All, TestMethod.AutomergeJava -> true
    TestMethod.AutomergeKotlin -> false
}

fun takeIfShouldTestJava(block: () -> List<RootTest>): List<RootTest> =
    if(RUN_AUTOMERGE_JAVA_TESTS) block() else emptyList()
