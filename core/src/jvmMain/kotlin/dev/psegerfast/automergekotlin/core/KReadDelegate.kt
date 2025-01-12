package dev.psegerfast.automergekotlin.core

import org.automerge.Read
import kotlin.jvm.optionals.getOrNull

class KReadDelegate(private val delegate: Read) : KRead {

    // Important to use get() here because the value will change between calls
    override val heads: Array<KChangeHash> get() =
        delegate.heads.toCommon()

    @Synchronized
    override fun get(obj: KObjectId, key: String): KAmValue? =
        delegate.get(obj.javaValue, key).getOrNull()?.toCommon()

    @Synchronized
    override fun get(obj: KObjectId, index: Int): KAmValue? =
        delegate.get(obj.javaValue, index).getOrNull()?.toCommon()

    @Synchronized
    override fun get(obj: KObjectId, key: String, heads: Array<KChangeHash>): KAmValue? =
        delegate.get(obj.javaValue, key, heads.toJava()).getOrNull()?.toCommon()

    @Synchronized
    override fun get(obj: KObjectId, index: Int, heads: Array<KChangeHash>): KAmValue? =
        delegate.get(obj.javaValue, index, heads.toJava()).getOrNull()?.toCommon()

    @Synchronized
    override fun text(obj: KObjectId): String? =
        delegate.text(obj.javaValue).getOrNull()

    @Synchronized
    override fun text(obj: KObjectId, heads: Array<KChangeHash>): String? =
        delegate.text(obj.javaValue, heads.toJava()).getOrNull()

    @Synchronized
    override fun keys(obj: KObjectId): List<String>? =
        delegate.keys(obj.javaValue).getOrNull()?.toList()

    @Synchronized
    override fun keys(obj: KObjectId, heads: Array<KChangeHash>): List<String>? =
        delegate.keys(obj.javaValue, heads.toJava()).getOrNull()?.toList()

    @Synchronized
    override fun mapEntries(obj: KObjectId): List<KMapEntry>? =
        delegate.mapEntries(obj.javaValue).getOrNull()?.commonEntries()

    @Synchronized
    override fun mapEntries(obj: KObjectId, heads: Array<KChangeHash>): List<KMapEntry>? =
        delegate.mapEntries(obj.javaValue, heads.toJava()).getOrNull()?.commonEntries()

    @Synchronized
    override fun length(obj: KObjectId): Long =
        delegate.length(obj.javaValue)

    @Synchronized
    override fun length(obj: KObjectId, heads: Array<KChangeHash>): Long =
        delegate.length(obj.javaValue, heads.toJava())

    @Synchronized
    override fun listItems(obj: KObjectId): List<KAmValue>? =
        delegate.listItems(obj.javaValue).getOrNull()?.map { it.toCommon() }

    @Synchronized
    override fun listItems(obj: KObjectId, heads: Array<KChangeHash>): List<KAmValue>? =
        delegate.listItems(obj.javaValue, heads.toJava()).getOrNull()?.map { it.toCommon() }

    @Synchronized
    override fun marks(obj: KObjectId): List<KMark>? =
        delegate.marks(obj.javaValue)?.toCommon()

    @Synchronized
    override fun marks(obj: KObjectId, heads: Array<KChangeHash>): List<KMark>? =
        delegate.marks(obj.javaValue, heads.toJava())?.toCommon()

    @Synchronized
    override fun getMarksAtIndex(obj: KObjectId, index: Int): Map<String, KAmValue> =
        delegate
            .getMarksAtIndex(obj.javaValue, index)
            .mapValues { (_, value) -> value.toCommon() }

    @Synchronized
    override fun getMarksAtIndex(obj: KObjectId, index: Int, heads: Array<KChangeHash>): Map<String, KAmValue> =
        delegate
            .getMarksAtIndex(obj.javaValue, index, heads.toJava())
            .mapValues { (_, value) -> value.toCommon() }

    @Synchronized
    override fun makeCursor(obj: KObjectId, index: Long): KCursor =
        delegate.makeCursor(obj.javaValue, index)

    @Synchronized
    override fun makeCursor(obj: KObjectId, index: Long, heads: Array<KChangeHash>): KCursor =
        delegate.makeCursor(obj.javaValue, index, heads.toJava())

    @Synchronized
    override fun lookupCursorIndex(obj: KObjectId, cursor: KCursor): Long =
        delegate.lookupCursorIndex(obj.javaValue, cursor)

    @Synchronized
    override fun lookupCursorIndex(obj: KObjectId, cursor: KCursor, heads: Array<KChangeHash>): Long =
        delegate.lookupCursorIndex(obj.javaValue, cursor, heads.toJava())

    @Synchronized
    override fun getObjectType(obj: KObjectId): KObjectType? =
        delegate.getObjectType(obj.javaValue).getOrNull()?.toCommon()

}