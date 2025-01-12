package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.automerge.MapEntry
import org.automerge.AmValue as JavaAmValue

actual typealias PlatformAmValue = JavaAmValue

fun JavaAmValue.toCommon() = when(this) {
    is JavaAmValue.UInt -> KAmValue.UInt(this)
    is JavaAmValue.Int -> KAmValue.Int(this)
    is JavaAmValue.Bool -> KAmValue.Bool(this)
    is JavaAmValue.Bytes -> KAmValue.Bytes(this)
    is JavaAmValue.Str -> KAmValue.Str(this)
    is JavaAmValue.F64 -> KAmValue.F64(this)
    is JavaAmValue.Counter -> KAmValue.Counter(this)
    is JavaAmValue.Timestamp -> KAmValue.Timestamp(this)
    is JavaAmValue.Null -> KAmValue.Null(this)
    is JavaAmValue.Unknown -> KAmValue.Unknown(this)
    is JavaAmValue.Map -> KAmValue.Map(this)
    is JavaAmValue.List -> KAmValue.List(this)
    is JavaAmValue.Text -> KAmValue.Text(this)
    else -> error("Unknown AmValue: $this")
}

fun Array<MapEntry>.commonEntries() = map { mapEntry ->
    KMapEntry(mapEntry.key, mapEntry.value.toCommon())
}

actual abstract class KAmValue {

    internal abstract val platformValue: PlatformAmValue

    /** An unsigned integer */
    actual class UInt(override val platformValue: JavaAmValue.UInt) : KAmValue() {
        actual val value: Long get() = platformValue.value
        override fun toString() = "UInt($value)"
    }

    /** A 64bit integer */
    actual class Int(override val platformValue: JavaAmValue.Int) : KAmValue() {
        actual val value: Long get() = platformValue.value
        override fun toString() = "Int($value)"
    }

    /** A Boolean */
    actual class Bool(override val platformValue: JavaAmValue.Bool) : KAmValue() {
        actual val value: Boolean get() = platformValue.value
        override fun toString() = "Bool($value)"
    }

    /** A byte array */
    actual class Bytes(override val platformValue: JavaAmValue.Bytes) : KAmValue() {
        actual val value: ByteArray get() = platformValue.value
        override fun toString() = "Bytes(${value.contentToString()})"
    }

    /** A string */
    actual class Str(override val platformValue: JavaAmValue.Str) : KAmValue() {
        actual val value: String get() = platformValue.value
        override fun toString() = "Str($value)"
    }

    /** A 64 bit floating point number */
    actual class F64(override val platformValue: JavaAmValue.F64) : KAmValue() {
        actual val value: Double get() = platformValue.value
        override fun toString() = "F64($value)"
    }

    /** A counter */
    actual class Counter(override val platformValue: JavaAmValue.Counter) : KAmValue() {
        actual val value: Long get() = platformValue.value
        override fun toString() = "Counter($value)"
    }

    /** A timestamp */
    actual class Timestamp(override val platformValue: JavaAmValue.Timestamp) : KAmValue() {
        actual val value: Instant get() = platformValue.value.toInstant().toKotlinInstant()
        override fun toString() = "Timestamp($value)"
    }

    /** A null value */
    actual class Null(override val platformValue: JavaAmValue.Null) : KAmValue() {
        override fun toString(): String = "Null"
    }

    /**
     * An unknown value
     *
     * This is used to represent values which may be added by future versions of
     * automerge.
     */
    actual class Unknown(override val platformValue: JavaAmValue.Unknown) : KAmValue() {
        actual val value: ByteArray get() = platformValue.value
        actual val typeCode get() = platformValue.typeCode
    }

    /** A map object */
    actual class Map(override val platformValue: JavaAmValue.Map) : KAmValue() {
        actual val id: KObjectId get() = platformValue.id.toCommon()
        override fun toString() = "Map($id)"
    }

    /** A list object */
    actual class List(override val platformValue: JavaAmValue.List) : KAmValue() {
        actual val id: KObjectId get() = platformValue.id.toCommon()
        override fun toString() = "List($id)"
    }

    /** A text object */
    actual class Text(override val platformValue: JavaAmValue.Text) : KAmValue() {
        actual val id: KObjectId get() = platformValue.id.toCommon()
        override fun toString() = "Text($id)"
    }

    companion object
}
