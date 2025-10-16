package dev.psegerfast.automergekotlin.core

import org.automerge.Patch

fun Patch.toCommon(): KPatch = KPatch(
    objectId = obj.toCommon(),
    path = path.map { it.toCommon() },
    action = action.toCommon(),
)
