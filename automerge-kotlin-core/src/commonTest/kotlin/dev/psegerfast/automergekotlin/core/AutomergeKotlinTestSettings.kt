package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest

val RUN_AUTOMERGE_KOTLIN_TESTS = when(TEST_METHOD) {
    TestMethod.All, TestMethod.AutomergeKotlin -> true
    TestMethod.AutomergeJava -> false
}

fun takeIfShouldTestKotlin(block: () -> List<RootTest>): List<RootTest> =
    if(RUN_AUTOMERGE_KOTLIN_TESTS) block() else emptyList()
