package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.util.Date
import kotlin.jvm.optionals.getOrNull
import org.automerge.Transaction as JavaTransaction

class KTransactionImpl internal constructor(
    private val platformTransaction: JavaTransaction
) : KTransaction, KReadable by KReadDelegate(platformTransaction) {
    override fun commit(): KChangeHash? =
        platformTransaction.commit().getOrNull()?.toCommon()

    override fun rollback() =
        platformTransaction.rollback()

    override fun set(obj: KObjectId, key: String, value: String) =
        platformTransaction.set(obj.javaValue, key, value)

    override fun set(obj: KObjectId, index: Long, value: String) =
        platformTransaction.set(obj.javaValue, index, value)

    override fun set(obj: KObjectId, key: String, value: Double) =
        platformTransaction.set(obj.javaValue, key, value)

    override fun set(obj: KObjectId, index: Long, value: Double) =
        platformTransaction.set(obj.javaValue, index, value)

    override fun set(obj: KObjectId, index: Long, value: Int) =
        platformTransaction.set(obj.javaValue, index, value)

    override fun set(obj: KObjectId, key: String, value: Int) =
        platformTransaction.set(obj.javaValue, key, value)

    override fun set(obj: KObjectId, key: String, value: KNewValue) =
        platformTransaction.set(obj.javaValue, key, value.javaValue())

    override fun set(obj: KObjectId, index: Long, value: KNewValue) =
        platformTransaction.set(obj.javaValue, index, value.javaValue())

    override fun set(obj: KObjectId, key: String, value: ByteArray) =
        platformTransaction.set(obj.javaValue, key, value)

    override fun set(obj: KObjectId, index: Long, value: ByteArray) =
        platformTransaction.set(obj.javaValue, index, value)

    override fun set(obj: KObjectId, key: String, value: Boolean) =
        platformTransaction.set(obj.javaValue, key, value)

    override fun set(obj: KObjectId, index: Long, value: Boolean) =
        platformTransaction.set(obj.javaValue, index, value)

    override fun set(obj: KObjectId, key: String, value: Instant) =
        platformTransaction.set(obj.javaValue, key, Date.from(value.toJavaInstant()))

    override fun set(obj: KObjectId, index: Long, value: Instant) =
        platformTransaction.set(obj.javaValue, index, Date.from(value.toJavaInstant()))

    override fun set(parent: KObjectId, key: String, objType: KObjectType): KObjectId =
        platformTransaction.set(parent.javaValue, key, objType.toJava()).toCommon()

    override fun set(parent: KObjectId, index: Long, objType: KObjectType): KObjectId =
        platformTransaction.set(parent.javaValue, index, objType.toJava()).toCommon()

    override fun setUint(obj: KObjectId, key: String, value: Long) =
        platformTransaction.setUint(obj.javaValue, key, value)

    override fun setUint(obj: KObjectId, index: Long, value: Long) =
        platformTransaction.setUint(obj.javaValue, index, value)

    override fun setNull(obj: KObjectId, key: String) =
        platformTransaction.setNull(obj.javaValue, key)

    override fun setNull(obj: KObjectId, index: Long) =
        platformTransaction.setNull(obj.javaValue, index)

    override fun insert(obj: KObjectId, index: Long, value: Double) =
        platformTransaction.insert(obj.javaValue, index, value)

    override fun insert(obj: KObjectId, index: Long, value: String) =
        platformTransaction.insert(obj.javaValue, index, value)

    override fun insert(obj: KObjectId, index: Long, value: Int) =
        platformTransaction.insert(obj.javaValue, index, value)

    override fun insert(obj: KObjectId, index: Long, value: ByteArray) =
        platformTransaction.insert(obj.javaValue, index, value)

    override fun insert(obj: KObjectId, index: Long, value: Instant) =
        platformTransaction.insert(obj.javaValue, index, Date.from(value.toJavaInstant()))

    override fun insert(obj: KObjectId, index: Long, value: Boolean) =
        platformTransaction.insert(obj.javaValue, index, value)

    override fun insert(obj: KObjectId, index: Long, value: KNewValue) =
        platformTransaction.insert(obj.javaValue, index, value.javaValue())

    override fun insert(parent: KObjectId, index: Long, objType: KObjectType): KObjectId =
        platformTransaction.insert(parent.javaValue, index, objType.toJava()).toCommon()

    override fun insertNull(obj: KObjectId, index: Long) =
        platformTransaction.insertNull(obj.javaValue, index)

    override fun insertUint(obj: KObjectId, index: Long, value: Long) =
        platformTransaction.insertUint(obj.javaValue, index, value)

    override fun increment(obj: KObjectId, key: String, amount: Long) =
        platformTransaction.increment(obj.javaValue, key, amount)

    override fun increment(obj: KObjectId, index: Long, amount: Long) =
        platformTransaction.increment(obj.javaValue, index, amount)

    override fun delete(obj: KObjectId, key: String) =
        platformTransaction.delete(obj.javaValue, key)

    override fun delete(obj: KObjectId, index: Long) =
        platformTransaction.delete(obj.javaValue, index)

    override fun splice(obj: KObjectId, start: Long, deleteCount: Long, items: List<KNewValue>) =
        platformTransaction.splice(obj.javaValue, start, deleteCount, items.map { it.javaValue() }.iterator())

    override fun spliceText(obj: KObjectId, start: Long, deleteCount: Long, text: String) =
        platformTransaction.spliceText(obj.javaValue, start, deleteCount, text)

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: KNewValue,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value.javaValue(), expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: String,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Long,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Double,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: ByteArray,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Instant,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, Date.from(value.toJavaInstant()), expand.toJava())

    override fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Boolean,
        expand: KExpandMark
    ) = platformTransaction.mark(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun markUint(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Long,
        expand: KExpandMark
    ) = platformTransaction.markUint(obj.javaValue, start, end, markName, value, expand.toJava())

    override fun markNull(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        expand: KExpandMark
    ) = platformTransaction.markNull(obj.javaValue, start, end, markName, expand.toJava())

    override fun unmark(
        obj: KObjectId,
        markName: String,
        start: Long,
        end: Long,
        expand: KExpandMark
    ) = platformTransaction.unmark(obj.javaValue, markName, start, end, expand.toJava())

    override fun close() = platformTransaction.close()

}

fun JavaTransaction.toCommon() = KTransactionImpl(this)