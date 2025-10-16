package dev.psegerfast.automergekotlin.core

expect class KObjectId {
    companion object {
        val ROOT: KObjectId
    }

    val isRoot: Boolean
}