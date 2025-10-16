package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestGetObjType : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testGetObjType() {
        val doc = KDocument()
        var map: KObjectId
        var list: KObjectId
        var text: KObjectId
        doc.startTransaction().use { tx ->
            map = tx.set(KObjectId.Companion.ROOT, "map", KObjectType.MAP)
            list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
            text = tx.set(KObjectId.Companion.ROOT, "text", KObjectType.TEXT)
            tx.commit()
        }
        // make an object ID from a different document
        val otherDoc = KDocument()
        var missingObj: KObjectId
        otherDoc.startTransaction().use { tx ->
            missingObj = tx.set(KObjectId.Companion.ROOT, "other", KObjectType.MAP)
            tx.commit()
        }
        Assertions.assertEquals(KObjectType.MAP, doc.getObjectType(map))
        Assertions.assertEquals(KObjectType.LIST, doc.getObjectType(list))
        Assertions.assertEquals(KObjectType.TEXT, doc.getObjectType(text))
        Assertions.assertEquals(null, doc.getObjectType(missingObj))

        doc.startTransaction().use { tx ->
            Assertions.assertEquals(KObjectType.MAP, tx.getObjectType(map))
            Assertions.assertEquals(KObjectType.LIST, tx.getObjectType(list))
            Assertions.assertEquals(KObjectType.TEXT, tx.getObjectType(text))
            Assertions.assertEquals(null, tx.getObjectType(missingObj))
        }
    }
}
