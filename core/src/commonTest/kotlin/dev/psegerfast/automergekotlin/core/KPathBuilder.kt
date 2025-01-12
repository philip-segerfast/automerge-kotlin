package dev.psegerfast.automergekotlin.core

internal class KPathBuilder {
    private val elements = ArrayList<KPathElement>()

    fun key(obj: KObjectId, key: String): KPathBuilder {
        elements.add(KPathElement(obj, KProp.Key(key)))
        return this
    }

    fun index(obj: KObjectId, idx: Long): KPathBuilder {
        elements.add(KPathElement(obj, KProp.Index(idx)))
        return this
    }

    fun build(): ArrayList<KPathElement> {
        return elements
    }

    companion object {
        fun empty(): ArrayList<KPathElement> {
            return ArrayList()
        }

        fun root(key: String): KPathBuilder {
            val pb = KPathBuilder()
            pb.elements.add(KPathElement(KObjectId.Companion.ROOT, KProp.Key(key)))
            return pb
        }
    }
}
