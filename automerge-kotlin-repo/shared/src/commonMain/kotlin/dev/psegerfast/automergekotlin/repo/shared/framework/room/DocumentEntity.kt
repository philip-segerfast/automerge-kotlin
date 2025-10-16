package dev.psegerfast.automergekotlin.repo.shared.framework.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.psegerfast.automergekotlin.repo.shared.DocumentId

@Entity("documents")
data class DocumentEntity(
    @PrimaryKey
    val documentId: DocumentId,
    val document: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentEntity

        if (!documentId.contentEquals(other.documentId)) return false
        if (!document.contentEquals(other.document)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = documentId.contentHashCode()
        result = 31 * result + document.contentHashCode()
        return result
    }

}
