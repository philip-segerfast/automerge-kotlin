package dev.psegerfast.automergekotlin.repo.shared.adapter.repository

import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.invoke
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.storage.DocumentStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DefaultDocumentRepository(
    private val syncer: DocumentSyncer,
    private val storage: DocumentStorage
) : DocumentRepository2 {

    private val scope = CoroutineScope(Dispatchers.Default)
    /** These "local" handles are purely in-memory. */
    private val _localHandles = mutableMapOf<DocumentId, DocHandle2>()

    override fun find(documentId: DocumentId): DocHandle2? {
        return _localHandles[documentId]
    }

    private fun ensureHandleNotExists(documentId: DocumentId) {
        if(_localHandles.containsKey(documentId)) {
            error("Document with id $documentId already exists.")
        }
    }

    override fun create(documentId: DocumentId): DocHandle2 {
        ensureHandleNotExists(documentId)

        val handleJob = Job().apply {
            invokeOnCompletion {
                println("CoroutineScope for DocHandle #$documentId was completed.")
            }
        }
        val handle = DefaultDocumentHandleImpl(
            document = KDocument(documentId),
            scope = CoroutineScope(scope.coroutineContext + handleJob),
        )
        _localHandles.put(documentId, handle)
        return handle
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun create(): DocHandle2 {
        val documentId = Uuid.random().toByteArray()
        return create(documentId)
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        suspend fun create(): DocumentRepository2 {
            println("Creating repository...")
            println("Creating rsocket connection...")
            val rsocket = RSocketFactory.create()

            val syncer = RSocketDocumentSyncer(
                rsocket = rsocket,
                proto = ProtoBuf.Default
            )

            val storage = object : DocumentStorage {
                override suspend fun read(documentId: DocumentId): ByteArray? = TODO()
                override suspend fun write(
                    documentId: DocumentId,
                    bytes: ByteArray
                ) = TODO()
                override suspend fun delete(documentId: DocumentId) = TODO()
            }

            println("Done!")

            return DefaultDocumentRepository(
                syncer = syncer,
                storage = storage,
            )
        }
    }
}