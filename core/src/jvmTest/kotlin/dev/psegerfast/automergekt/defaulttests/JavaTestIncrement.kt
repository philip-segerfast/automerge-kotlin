package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.Transaction

// TODO - Tests were uncommented because the Counter class is package-private in Automerge.
class JavaTestIncrement : AnnotationSpec() {
    private var doc: Document? = null
    private var tx: Transaction? = null

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

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
