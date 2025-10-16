package dev.psegerfast.automergekotlin.core

import org.automerge.ChangeHash as JavaChangeHash

@JvmInline
internal value class KChangeHashImpl(val value: JavaChangeHash) : KChangeHash {
    override val bytes: ByteArray get() = value.bytes
}

internal fun Array<KChangeHash>.toJava(): Array<JavaChangeHash> = map { (it as KChangeHashImpl).value }.toTypedArray()

internal fun JavaChangeHash.toCommon() = KChangeHashImpl(this)

internal fun Array<JavaChangeHash>.toCommon(): Array<KChangeHash> = map { it.toCommon() }.toTypedArray()
