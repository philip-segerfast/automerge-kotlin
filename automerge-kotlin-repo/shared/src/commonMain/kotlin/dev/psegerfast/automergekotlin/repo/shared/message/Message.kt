package dev.psegerfast.automergekotlin.repo.shared.message

import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {
    val type: String
}

@Serializable
class DocumentUnavailableMessage : Message {
    override val type: String = "doc-unavailable"
}

/**
 * Used to both push a new document.proto and to update an existing document.proto.
 * */
@Serializable
data class SyncMessage(val documentId: DocumentId, val message: ByteArray?) : Message {
    override val type: String = "sync"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SyncMessage

        if (!documentId.contentEquals(other.documentId)) return false
        if (!message.contentEquals(other.message)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = documentId.contentHashCode()
        result = 31 * result + (message?.contentHashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }
}

@Serializable
data class StartSyncSessionMessage(
    val documentId: DocumentId,
) : Message {
    override val type: String = "start-sync-session"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StartSyncSessionMessage

        if (!documentId.contentEquals(other.documentId)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = documentId.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}