package dev.psegerfast.automergekotlin.core

import org.automerge.PathElement

fun PathElement.toCommon() = KPathElement(
    objectId.toCommon(),
    prop.toCommon(),
)
