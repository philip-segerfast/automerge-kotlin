package dev.psegerfast.automergekotlin.repo.shared.dochandle

import dev.psegerfast.automergekotlin.core.KReadable
import dev.psegerfast.automergekotlin.core.KTransaction
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.coroutines.flow.SharedFlow

interface DocHandle2 {
    val documentId: DocumentId
    val deleted: Boolean
    val data: SharedFlow<KReadable?>

    suspend fun change(block: (KTransaction) -> Unit)
    suspend fun delete()
}