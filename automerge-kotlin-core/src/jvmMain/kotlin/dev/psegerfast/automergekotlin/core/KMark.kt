package dev.psegerfast.automergekotlin.core

import org.automerge.Mark

@JvmInline
value class KMarkImpl(internal val javaValue: Mark) : KMark {
    override val start: Long get() = javaValue.start

    override val end: Long get() = javaValue.end

    override val name: String get() = javaValue.name

    override val value: KAmValue get() = javaValue.value.toCommon()

    override fun toString(): String = javaValue.toString()
}

fun Mark.toCommon(): KMark = KMarkImpl(this)

fun List<Mark>.toCommon(): List<KMark> = map { it.toCommon() }
