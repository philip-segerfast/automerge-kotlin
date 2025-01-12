package dev.psegerfast.automergekotlin.repo.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.psegerfast.automergekotlin.core.treeString
import dev.psegerfast.repo.DocumentHandle
import dev.psegerfast.repo.MainDocumentRepository

fun main() = application {
    val mainRepository = MainDocumentRepository.create()
    val documentId = "testDocument"
    val docHandleLeft = remember { mainRepository.find(documentId) }
    val docHandleRight = remember { mainRepository.find(documentId) }

    Window(onCloseRequest = ::exitApplication) {
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f)) {
                DocumentTestArea(
                    modifier = Modifier.fillMaxSize(),
                    docHandle = docHandleLeft
                )
            }
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.Blue)
            )
            Column(Modifier.weight(1f)) {
                DocumentTestArea(
                    modifier = Modifier.fillMaxSize(),
                    docHandle = docHandleRight
                )
            }
        }
    }
}

@Composable
fun DocumentTestArea(
    modifier: Modifier = Modifier,
    docHandle: DocumentHandle,
) {
    val handleState by docHandle.handleState.collectAsState()
    val docSnapshot by docHandle.currentSnapshot.collectAsState()

    Column {
        Text("Handle state: $handleState")
        Text("Document snapshot: ${ docSnapshot?.treeString() }")
    }
}