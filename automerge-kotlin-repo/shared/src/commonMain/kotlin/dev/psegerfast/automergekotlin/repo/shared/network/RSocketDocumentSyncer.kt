package dev.psegerfast.automergekotlin.repo.shared.network

import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.decodeFromPayload
import dev.psegerfast.automergekotlin.repo.shared.docsync.DocumentSyncer
import dev.psegerfast.automergekotlin.repo.shared.encodeToPayload
import dev.psegerfast.automergekotlin.repo.shared.message.Message
import dev.psegerfast.automergekotlin.repo.shared.message.StartSyncSessionMessage
import dev.psegerfast.automergekotlin.repo.shared.message.SyncMessage
import io.rsocket.kotlin.RSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(DelicateCoroutinesApi::class, ExperimentalUuidApi::class)
class RSocketDocumentSyncer @OptIn(ExperimentalSerializationApi::class) constructor(
    private val rsocket: RSocket,
    private val proto: ProtoBuf,
) : DocumentSyncer {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun startSync(documentId: DocumentId): Flow<Message> {
        val payload = proto.encodeToPayload(
            value = StartSyncSessionMessage(documentId = documentId)
        )

        val stream = rsocket.requestStream(payload)

        val response = stream.map { proto.decodeFromPayload<SyncMessage>(it) }
        return response
    }

    override suspend fun sendSyncMessage(
        documentId: DocumentId,
        message: SyncMessage
    ) {
        TODO("Not yet implemented")
    }

    init {
        GlobalScope.launch {
            val syncFlow = startSync(Uuid.Companion.parse("9903d698-8e3a-4ebe-b357-f7971a4c42e3").toByteArray())
            syncFlow.collect {
                println("Received sync message: $it")
            }
        }
    }

}