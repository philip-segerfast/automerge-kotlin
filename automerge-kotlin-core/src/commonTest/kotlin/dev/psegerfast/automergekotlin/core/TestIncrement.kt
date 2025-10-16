package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec

// TODO - Tests were uncommented because the Counter class is package-private in Automerge.
class TestIncrement : AnnotationSpec() {
    private var doc: KDocument? = null
    private var tx: KTransaction? = null

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

//    @BeforeEach
//    fun setup() {
//        doc = Document()
//        tx = doc!!.startTransaction()
//    }
//
//    @Test
//    fun testIncrementInMap() {
//        tx!![ObjectId.ROOT, "key"] = Counter(10)
//        tx!!.increment(ObjectId.ROOT, "key", 5)
//        Assertions.assertEquals(15, (doc!![ObjectId.ROOT, "key"].get() as AmValue.Counter).value)
//    }
//
//    @Test
//    fun testIncrementInList() {
//        val list = tx!!.set(ObjectId.ROOT, "list", ObjectType.LIST)
//        tx!!.insert(list, 0, Counter(10))
//        tx!!.increment(list, 0, 5)
//        Assertions.assertEquals(15, (doc!![list, 0].get() as AmValue.Counter).value)
//    }
}
