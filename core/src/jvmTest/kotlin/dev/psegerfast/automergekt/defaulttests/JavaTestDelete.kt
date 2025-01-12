package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.NewValue
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Transaction
import org.junit.jupiter.api.Assertions
import java.util.Optional

class JavaTestDelete : AnnotationSpec() {
    private lateinit var doc: Document
    private lateinit var tx: Transaction

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @BeforeEach
    fun setup() {
        doc = Document()
        tx = doc.startTransaction()
    }

    @Test
    fun testDeleteInMap() {
        tx[ObjectId.ROOT, "key"] = NewValue.str("Hello") // TODO - Changed from Counter from original Java tests.
        tx.delete(ObjectId.ROOT, "key")
        Assertions.assertEquals(tx[ObjectId.ROOT, "key"], Optional.empty<Any>())
    }

    @Test
    fun testDeleteInList() {
        val list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
        tx.insert(list, 0, 123)
        tx.delete(list, 0)
        Assertions.assertEquals(tx[list, 0], Optional.empty<Any>())
    }
} // Check that committing the transaction clears Document.transactionPtr
