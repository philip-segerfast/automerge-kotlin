package dev.psegerfast.automergekotlin.core

import org.automerge.Prop

fun Prop.toCommon() = when(this) {
    is Prop.Key -> KProp.Key(key)
    is Prop.Index -> KProp.Index(index)
    else -> error("Unsupported prop: $this")
}