package dev.psegerfast.automergekotlin.core

import org.automerge.PatchAction

fun PatchAction.toCommon(): KPatchAction = when(this) {
    is PatchAction.PutMap -> KPatchAction.PutMap(key, value.toCommon(), isConflict)
    is PatchAction.PutList -> KPatchAction.PutList(index, value.toCommon(), isConflict)
    is PatchAction.Insert -> KPatchAction.Insert(index, values.map { it.toCommon() })
    is PatchAction.SpliceText -> KPatchAction.SpliceText(index, text)
    is PatchAction.Increment -> KPatchAction.Increment(property.toCommon(), value)
    is PatchAction.DeleteMap -> KPatchAction.DeleteMap(key)
    is PatchAction.DeleteList -> KPatchAction.DeleteList(index, length)
    is PatchAction.Mark -> KPatchAction.Mark(marks.map { it.toCommon() })
    // TODO - Is this a bug?
    is PatchAction.FlagConflict -> KPatchAction.FlagConflict(property.toCommon())
    else -> error("Unsupported")
}