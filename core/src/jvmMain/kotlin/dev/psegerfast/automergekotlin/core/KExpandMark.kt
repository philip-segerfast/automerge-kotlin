package dev.psegerfast.automergekotlin.core

import org.automerge.ExpandMark

fun KExpandMark.toJava() = when(this) {
    KExpandMark.BEFORE -> ExpandMark.BEFORE
    KExpandMark.AFTER -> ExpandMark.AFTER
    KExpandMark.BOTH -> ExpandMark.BOTH
    KExpandMark.NONE -> ExpandMark.NONE
}
