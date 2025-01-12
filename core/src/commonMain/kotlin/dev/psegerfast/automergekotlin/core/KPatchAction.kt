package dev.psegerfast.automergekotlin.core

sealed interface KPatchAction {
    data class PutMap(
        val key: String,
        val value: KAmValue,
        val isConflict: Boolean,
    ) : KPatchAction

    data class PutList(
        val index: Long,
        val value: KAmValue,
        val isConflict: Boolean,
    ) : KPatchAction

    data class Insert(
        val index: Long,
        val values: List<KAmValue>,
    ) : KPatchAction

    data class SpliceText(
        val index: Long,
        val text: String,
    ) : KPatchAction

    data class Increment(
        val property: KProp,
        val value: Long,
    ) : KPatchAction

    data class DeleteMap(
        val key: String,
    ) : KPatchAction

    data class DeleteList(
        val index: Long,
        val length: Long,
    ) : KPatchAction

    data class Mark(
        val marks: List<KMark>,
    ) : KPatchAction

    data class FlagConflict(
        val property: KProp,
    ) : KPatchAction
}