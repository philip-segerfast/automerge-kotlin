package com.example

import dev.psegerfast.automergekotlin.repo.shared.encodeToPayload
import dev.psegerfast.automergekotlin.repo.shared.message.SyncMessage
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.ktor.server.RSocketSupport
import io.rsocket.kotlin.ktor.server.rSocket
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.flow
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureRSocket()
}

fun Application.configureRSocket() {
    install(WebSockets)
    install(RSocketSupport)

    rsocketRouting()
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
fun Application.rsocketRouting() {
    routing {
        rSocket("rsocket") {
            println(config.setupPayload.data.readString())

            RSocketRequestHandler {
                requestStream { request: Payload ->
                    println("Received payload: ${ request.data.readString() }")
                    println("Returning flow...")

                    flow {
                        repeat(3) {
                            val msg = SyncMessage(
                                documentId = Uuid.parse("9903d698-8e3a-4ebe-b357-f7971a4c42e3").toByteArray(),
                                message = byteArrayOf(1,2,3,4,5)
                            )

                            val payload = ProtoBuf.Default.encodeToPayload(msg)

                            println("Sending message: $msg")
                            emit(payload)
                        }
                    }
                }

            }
        }
    }
}