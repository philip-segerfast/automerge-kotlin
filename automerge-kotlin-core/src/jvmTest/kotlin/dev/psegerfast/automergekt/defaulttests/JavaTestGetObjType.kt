package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.junit.jupiter.api.Assertions
import java.util.Optional

internal class JavaTestGetObjType : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testGetObjType() {
        val doc = Document()
        var map: ObjectId
        var list: ObjectId
        var text: ObjectId
        doc.startTransaction().use { tx ->
            map = tx.set(ObjectId.ROOT, "map", ObjectType.MAP)
            list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
            text = tx.set(ObjectId.ROOT, "text", ObjectType.TEXT)
            tx.commit()
        }
        // make an object ID from a different document
        val otherDoc = Document()
        var missingObj: ObjectId
        otherDoc.startTransaction().use { tx ->
            missingObj = tx.set(ObjectId.ROOT, "other", ObjectType.MAP)
            tx.commit()
        }
        Assertions.assertEquals(Optional.of(ObjectType.MAP), doc.getObjectType(map))
        Assertions.assertEquals(Optional.of(ObjectType.LIST), doc.getObjectType(list))
        Assertions.assertEquals(Optional.of(ObjectType.TEXT), doc.getObjectType(text))
        Assertions.assertEquals(Optional.empty<Any>(), doc.getObjectType(missingObj))

        doc.startTransaction().use { tx ->
            Assertions.assertEquals(Optional.of(ObjectType.MAP), tx.getObjectType(map))
            Assertions.assertEquals(Optional.of(ObjectType.LIST), tx.getObjectType(list))
            Assertions.assertEquals(Optional.of(ObjectType.TEXT), tx.getObjectType(text))
            Assertions.assertEquals(Optional.empty<Any>(), tx.getObjectType(missingObj))
        }
    }
}
