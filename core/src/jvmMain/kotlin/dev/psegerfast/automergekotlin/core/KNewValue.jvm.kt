package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.automerge.NewValue as JavaNewValue
import java.util.Date

internal fun KNewValue.javaValue(): JavaNewValue = (this as KNewValueImpl).value

@JvmInline
internal value class KNewValueImpl(val value: JavaNewValue) : KNewValue {
    override fun toString(): String = "KNewValueImpl(value=$value)"
}

actual interface KNewValue {
    actual companion object {
        actual fun uint(value: Long): KNewValue =
            KNewValueImpl(JavaNewValue.uint(value))

        actual fun integer(value: Long): KNewValue =
            KNewValueImpl(JavaNewValue.integer(value))

        actual fun f64(value: Double): KNewValue =
            KNewValueImpl(JavaNewValue.f64(value))

        actual fun bool(value: Boolean): KNewValue =
            KNewValueImpl(JavaNewValue.bool(value))

        actual fun str(value: String): KNewValue =
            KNewValueImpl(JavaNewValue.str(value))

        actual fun bytes(value: ByteArray): KNewValue =
            KNewValueImpl(JavaNewValue.bytes(value))

        actual fun counter(value: Long): KNewValue =
            KNewValueImpl(JavaNewValue.counter(value))

        actual fun timestamp(value: Instant): KNewValue =
            KNewValueImpl(JavaNewValue.timestamp(Date.from(value.toJavaInstant())))

        actual val Null: KNewValue
            get() =
            KNewValueImpl(JavaNewValue.NULL)
    }
}
