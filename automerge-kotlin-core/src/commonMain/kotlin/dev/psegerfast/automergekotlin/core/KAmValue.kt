package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant

expect abstract class PlatformAmValue

expect abstract class KAmValue {

    /** An unsigned integer */
    class UInt : KAmValue {
        val value: Long
    }

    /** A 64bit integer */
    class Int : KAmValue {
        val value: Long
    }

    /** A Boolean */
    class Bool : KAmValue {
        val value: Boolean
    }

    /** A byte array */
    class Bytes : KAmValue {
        val value: ByteArray
    }

    /** A string */
    class Str : KAmValue {
        val value: String
    }

    /** A 64 bit floating point number */
    class F64 : KAmValue {
        val value: Double
    }

    /** A counter */
    class Counter : KAmValue {
        val value: Long
    }

    /** A timestamp */
    class Timestamp : KAmValue {
        val value: Instant
    }

    /** A null value */
    class Null : KAmValue

    /**
     * An unknown value
     *
     * <p>
     * This is used to represent values which may be added by future versions of
     * automerge.
     */
    class Unknown : KAmValue {
        val value: ByteArray
        val typeCode: kotlin.Int
    }

    /** A map object */
    class Map : KAmValue {
        val id: KObjectId
    }

    /** A list object */
    class List : KAmValue {
        val id: KObjectId
    }

    /** A text object */
    class Text : KAmValue {
        val id: KObjectId
    }

}


