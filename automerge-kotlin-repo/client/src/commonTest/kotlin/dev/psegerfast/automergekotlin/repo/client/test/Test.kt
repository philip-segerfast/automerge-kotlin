package dev.psegerfast.automergekotlin.repo.client.test

import dev.psegerfast.automergekotlin.repo.client.MainDocumentRepository
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Suppress("unused")
@OptIn(ExperimentalUuidApi::class)
class RepoTest : FunSpec({

    val handleId = Uuid.parse("eb0ed623-7ed0-4121-9c11-474778ca9983").toByteArray()

    beforeTest { test ->
        println("Executing test: ${test.name.testName}")
    }

    test("Create repo") {
        val repo = MainDocumentRepository.create()
    }

    test("Create DocumentHandle") {
        val repo = MainDocumentRepository.create()

        val handle = repo.getOrCreate(handleId)

        coroutineScope {
            launch {
                handle.currentSnapshot.collectLatest { snapshot ->
                    println("Snapshot: $snapshot")
                }
            }
        }
    }

})