package dev.psegerfast.automergekotlin.repo.shared

import dev.psegerfast.automergekotlin.core.KChangeHash
import kotlinx.serialization.Serializable

typealias DocumentId = ByteArray

object ApiPayloads {
    object StreamDocumentChanges {
        @Serializable
        class SetCurrentHeadsPayload(
            val documentId: DocumentId,
            val heads: Array<KChangeHash>,
        )

        @Serializable
        class ReceiveChangesResponse(
            val changes: ByteArray,
        )
    }
}