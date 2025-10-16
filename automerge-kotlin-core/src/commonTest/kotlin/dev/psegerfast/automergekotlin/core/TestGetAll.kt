package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import kotlin.text.set

// TODO - All Conflicts-related code has been commented out due to it being package-private

internal class TestGetAll : AnnotationSpec() {
    internal interface TestCase {
        fun init(tx: KTransaction, value: Double)

        fun branch1(tx: KTransaction, value: Double)

        fun branch2(tx: KTransaction, value: Double)

        fun merge(tx: KTransaction, value: Double)

//        fun getConflicts(doc: Read): Conflicts

//        fun getConflictsAt(doc: Read, heads: Array<ChangeHash?>?): Conflicts
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testGetAllInMap() {
        runMap { doc: KDocument -> doc.startTransaction() }
    }

    @Test
    fun testGetAllInList() {
        runList { doc: KDocument -> doc.startTransaction() }
    }

    private fun runMap(createTx: (KDocument) -> KTransaction) {
        run(object : TestCase {
            override fun init(tx: KTransaction, value: Double) {
                tx[KObjectId.Companion.ROOT, "key"] = value
            }

            override fun branch1(tx: KTransaction, value: Double) {
                tx[KObjectId.Companion.ROOT, "key"] = value
            }

            override fun branch2(tx: KTransaction, value: Double) {
                tx[KObjectId.Companion.ROOT, "key"] = value
            }

            override fun merge(tx: KTransaction, value: Double) {
                tx[KObjectId.Companion.ROOT, "key"] = value
            }

//            override fun getConflicts(doc: Read): Conflicts {
//                return doc.getAll(KObjectId.ROOT, "key").get()
//            }

//            override fun getConflictsAt(doc: Read, heads: Array<ChangeHash?>?): Conflicts {
//                return doc.getAll(KObjectId.ROOT, "key", heads).get()
//            }
        }, createTx)
    }

    fun runList(createTx: (KDocument) -> KTransaction) {
        run(object : TestCase {
            lateinit var list: KObjectId

            override fun init(tx: KTransaction, value: Double) {
                list = tx.set(KObjectId.Companion.ROOT, "list", KObjectType.LIST)
                tx.insert(list, 0, value)
            }

            override fun branch1(tx: KTransaction, value: Double) {
                tx[list, 0] = value
            }

            override fun branch2(tx: KTransaction, value: Double) {
                tx[list, 0] = value
            }

            override fun merge(tx: KTransaction, value: Double) {
                tx[list, 0] = value
            }

//            override fun getConflicts(doc: Read): Conflicts {
//                return doc.getAll(list, 0).get()
//            }

//            override fun getConflictsAt(doc: Read, heads: Array<ChangeHash?>?): Conflicts {
//                return doc.getAll(list, 0, heads).get()
//            }
        }, createTx)
    }

    fun run(testCase: TestCase, createTx: (KDocument) -> KTransaction) {
        val doc = KDocument()
        createTx(doc).use { tx ->
            testCase.init(tx, 1.23)
            tx.commit()
        }
        val doc2 = doc.fork()
        createTx(doc).use { tx ->
            testCase.branch1(tx, 4.56)
            tx.commit()
        }
        createTx(doc2).use { tx ->
            testCase.branch2(tx, 7.89)
            tx.commit()
        }
        doc.merge(doc2)
//        val heads = doc.heads
        // Check it works with an open transaction
        var tx = createTx(doc)
//        var conflicts = testCase.getConflicts(doc)
//        assertConflicts(conflicts)

        // check the version from the transaction works
//        conflicts = testCase.getConflicts(tx)
//        assertConflicts(conflicts)

        // Check works without an open transaction
        tx.commit()
//        conflicts = testCase.getConflicts(doc)
//        assertConflicts(conflicts)

        createTx(doc).use { tx2 ->
            testCase.merge(tx2, 2.00)
            tx2.commit()
        }
        // Check there are no conflicts after the merge
//        conflicts = testCase.getConflicts(doc)
//        Assertions.assertEquals(1, conflicts.values().size)

        // Check getAllAt works with an open transaction
        tx = createTx(doc)
//        conflicts = testCase.getConflictsAt(doc, heads)
//        assertConflicts(conflicts)

        // check the version from the transaction works
//        conflicts = testCase.getConflictsAt(tx, heads)
//        assertConflicts(conflicts)

        // Check works without an open transaction
        tx.commit()
//        conflicts = testCase.getConflictsAt(doc, heads)
//        assertConflicts(conflicts)
    }

//    fun assertConflicts(conflicts: Conflicts) {
//        Assertions.assertEquals(2, conflicts.values().size)
//
//        val expected = HashSet<Double>()
//        expected.add(4.56)
//        expected.add(7.89)
//
//        val values = HashSet<Double>()
//        for (value in conflicts.values()) {
//            values.add((value as AmValue.F64).value)
//        }
//        Assertions.assertEquals(expected, values)
//    }
}
