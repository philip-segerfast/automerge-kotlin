package dev.psegerfast.automergekotlin.core

import org.automerge.ObjectId

actual class KObjectId internal constructor(internal val javaValue: ObjectId) {
    actual companion object {
        actual val ROOT = KObjectId(ObjectId.ROOT)
    }

    actual val isRoot: Boolean get() = javaValue.isRoot

    override fun toString(): String {
        return javaValue.toString()
    }

    override fun hashCode(): Int {
        return javaValue.hashCode()
    }

    /**
     * If KObjectId and a ObjectId are compared, the inner javaValue is compared with ObjectId.
     * */
    // TODO - Unsure if this is a good idea...
    override fun equals(other: Any?): Boolean {
        if(this === other) return true

        return when (other) {
            is KObjectId -> other.javaValue == javaValue
            is ObjectId -> other == javaValue
            else -> false
        }
    }
}

internal fun ObjectId.toCommon(): KObjectId = KObjectId(this)
