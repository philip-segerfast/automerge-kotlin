package dev.psegerfast.automergekotlin.repo.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.psegerfast.automergekotlin.core.KObjectId
import dev.psegerfast.automergekotlin.core.KObjectType
import dev.psegerfast.automergekotlin.core.treeString
import dev.psegerfast.automergekotlin.repo.client.DisposableDocumentHandle
import dev.psegerfast.automergekotlin.repo.client.DocumentRepository
import dev.psegerfast.automergekotlin.repo.client.new.DefaultDocumentRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
suspend fun main() = coroutineScope {

    val docId = Uuid.parse("9903d698-8e3a-4ebe-b357-f7971a4c42e3").toByteArray()
    println("Getting repo...")
    val repo = DefaultDocumentRepository.create()
    println("Done!")
    val handle1 = repo.create(docId)
    val handle2 = repo.find(docId)!!

    var textId: KObjectId? by mutableStateOf(null)

    println("Handle #1: $handle1")
    println("Handle #2: $handle2")

    fun change1() {
        launch {
            handle1.change { tx ->
                tx.spliceText(textId!!, 0, 0, "hello")
            }
        }
    }

    fun change2() {
        launch {
            handle2.change { tx ->
                tx.spliceText(textId!!, 0, 0, "hello")
            }
        }
    }

    fun init1() {
        launch {
            handle1.change { tx ->
                textId = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            }
        }
    }

    application {
        val scope = rememberCoroutineScope()

        scope.launch {
            coroutineScope {
                launch {
                    var counter = 0
                    handle1.data.collect { data ->
                        println("[$counter] Handle1 data: ${ data?.treeString() }")
                        counter++
                    }
                }

                launch {
                    var counter = 0
                    handle2.data.collect { data ->
                        println("[$counter] Handle2 data: ${ data?.treeString() }")
                        counter++
                    }
                }
            }
        }

        Window(onCloseRequest = ::exitApplication) {
            Row(Modifier.fillMaxSize()) {
                if(textId != null) {
                    Button(onClick = ::change1) {
                        Text("Change docHandle1")
                    }
                }

                Button(onClick = ::change2) {
                    Text("Change docHandle2")
                }

                Button(onClick = ::init1) {
                    Text("Init DocHandle1")
                }

//            Column(Modifier.weight(1f)) {
//                DocumentTestArea(
//                    modifier = Modifier.fillMaxSize(),
//                    docHandle = docHandleLeft
//                )
//            }
//            Box(
//                Modifier
//                    .fillMaxHeight()
//                    .width(1.dp)
//                    .background(Color.Blue)
//            )
//            Column(Modifier.weight(1f)) {
//                DocumentTestArea(
//                    modifier = Modifier.fillMaxSize(),
//                    docHandle = docHandleRight
//                )
//            }
            }
        }
    }
}

@Composable
fun DocumentTestArea(
    modifier: Modifier = Modifier,
    docHandle: DisposableDocumentHandle,
) {
    val docSnapshot by docHandle.currentSnapshot.collectAsState()

    Column {
        Text("Document snapshot: ${ docSnapshot?.treeString() }")
    }
}