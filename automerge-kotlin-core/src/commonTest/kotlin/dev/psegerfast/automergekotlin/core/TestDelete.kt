package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions
import kotlin.text.set

class TestDelete : AnnotationSpec() {
    private lateinit var doc: KDocument
    private lateinit var tx: KTransaction

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = KDocument()
        tx = doc.startTransaction()
    }

    @Test
    fun testDeleteInMap() {
        tx[KObjectId.Companion.ROOT, "key"] = KNewValue.Companion.str("hello") // TODO - Changed from Counter from original Java tests.
        tx.delete(KObjectId.Companion.ROOT, "key")
        Assertions.assertEquals(tx[KObjectId.Companion.ROOT, "key"], null)
    }

    @Test
    fun testDeleteInList() {
        val list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
        tx.insert(list, 0, 123)
        tx.delete(list, 0)
        Assertions.assertEquals(tx[list, 0], null)
    }
} // Check that committing the transaction clears KDocument.transactionPtr
