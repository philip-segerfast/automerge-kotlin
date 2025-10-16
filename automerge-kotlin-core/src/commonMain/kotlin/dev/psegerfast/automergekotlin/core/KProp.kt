package dev.psegerfast.automergekotlin.core

sealed interface KProp {
    data class Key(val key: String) : KProp
    data class Index(val index: Long) : KProp
}
