package dev.psegerfast.automergekotlin.core

data class KPatch(
    val objectId: KObjectId,
    val path: List<KPathElement>,
    val action: KPatchAction
)
