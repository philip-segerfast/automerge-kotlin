package dev.psegerfast.automergekotlin.core

import org.automerge.ObjectType
import org.automerge.ObjectType as JavaObjectType

fun KObjectType.toJava(): JavaObjectType = when (this) {
    KObjectType.MAP -> JavaObjectType.MAP
    KObjectType.LIST -> JavaObjectType.LIST
    KObjectType.TEXT -> JavaObjectType.TEXT
}

fun JavaObjectType.toCommon(): KObjectType = when (this) {
    ObjectType.MAP -> KObjectType.MAP
    ObjectType.LIST -> KObjectType.LIST
    ObjectType.TEXT -> KObjectType.TEXT
}
