package dev.psegerfast.automergekt.defaulttests

import org.automerge.ObjectId
import org.automerge.PathElement
import org.automerge.Prop

class MyPathElement(obj: ObjectId?, prop: Prop?): PathElement(obj, prop) {
    constructor(pathElement: PathElement) : this(pathElement.objectId, pathElement.prop)
}

fun PathElement.toMyPathElement(): MyPathElement = MyPathElement(this)

internal class KPathBuilder {
    private val elements = ArrayList<PathElement>()

    fun key(obj: ObjectId?, key: String?): KPathBuilder {
        elements.add(MyPathElement(obj, Prop.Key(key)))
        return this
    }

    fun index(obj: ObjectId?, idx: Long): KPathBuilder {
        elements.add(MyPathElement(obj, Prop.Index(idx)))
        return this
    }

    fun build(): ArrayList<PathElement> {
        return elements
    }

    companion object {
        fun empty(): ArrayList<PathElement> {
            return ArrayList()
        }

        fun root(key: String?): KPathBuilder {
            val pb = KPathBuilder()
            pb.elements.add(MyPathElement(ObjectId.ROOT, Prop.Key(key)))
            return pb
        }
    }
}
