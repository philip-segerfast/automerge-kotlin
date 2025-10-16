package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant

expect interface KNewValue {
    companion object {
        /**
         * Create a new unsigned integer value
         *
         * @param value
         * the positive integer value
         * @return a new unsigned integer value
         * @throws IllegalArgumentException
         * if the value is negative
         */
        fun uint(value: Long): KNewValue

        /**
         * A new integer value
         *
         * @param value
         * the integer value
         * @return a new integer value
         */
        fun integer(value: Long): KNewValue

        /**
         * A new floating point value
         *
         * @param value
         * the floating point value
         * @return a new floating point value
         */
        fun f64(value: Double): KNewValue

        /**
         * A new boolean value
         *
         * @param value
         * the boolean value
         * @return a new boolean value
         */
        fun bool(value: Boolean): KNewValue

        /**
         * A new string value
         *
         * @param value
         * the string value
         * @return a new string value
         */
        fun str(value: String): KNewValue

        /**
         * A new byte array value
         *
         * @param value
         * the byte array value
         * @return a new byte array value
         */
        fun bytes(value: ByteArray): KNewValue

        /**
         * A new counter value
         *
         * @param value
         * the initial value of the counter
         * @return a new counter value
         */
        fun counter(value: Long): KNewValue

        /**
         * A new timestamp value
         *
         * @param value
         * the value of the timestamp
         * @return a new timestamp value
         */
        fun timestamp(value: Instant): KNewValue

        val Null: KNewValue
    }
}

///**
// * Any non-object value to be added to a document
// *
// * Occasionally you may need to be generic over the kind of new value to insert
// * into a document (for example in
// * [Transaction.set]). This class encapsulates
// * all the possible non-object values one can create. You should use one of the
// * static methods on NewValue to create an instance.
// */
//expect class KNewValue {
//    companion object {
//        /**
//         * Create a new unsigned integer value
//         *
//         * @param value
//         * the positive integer value
//         * @return a new unsigned integer value
//         * @throws IllegalArgumentException
//         * if the value is negative
//         */
//        fun KNewValue.uint(value: Long): KNewValue
//
//        /**
//         * A new integer value
//         *
//         * @param value
//         * the integer value
//         * @return a new integer value
//         */
//        fun KNewValue.integer(value: Long): KNewValue
//
//        /**
//         * A new floating point value
//         *
//         * @param value
//         * the floating point value
//         * @return a new floating point value
//         */
//        fun KNewValue.f64(value: Double): KNewValue
//
//        /**
//         * A new boolean value
//         *
//         * @param value
//         * the boolean value
//         * @return a new boolean value
//         */
//        fun KNewValue.bool(value: Boolean): KNewValue
//
//        /**
//         * A new string value
//         *
//         * @param value
//         * the string value
//         * @return a new string value
//         */
//        fun KNewValue.str(value: String): KNewValue
//
//        /**
//         * A new byte array value
//         *
//         * @param value
//         * the byte array value
//         * @return a new byte array value
//         */
//        fun KNewValue.bytes(value: ByteArray): KNewValue
//
//        /**
//         * A new counter value
//         *
//         * @param value
//         * the initial value of the counter
//         * @return a new counter value
//         */
//        fun KNewValue.counter(value: Long): KNewValue
//
//        /**
//         * A new timestamp value
//         *
//         * @param value
//         * the value of the timestamp
//         * @return a new timestamp value
//         */
//        fun KNewValue.timestamp(value: Date): KNewValue
//
//        val KNewValue.Null: KNewValue
//    }
//}
