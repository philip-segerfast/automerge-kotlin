package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.ObjectType
import org.automerge.Transaction
import java.util.function.Function

// TODO - All Conflicts-related code has been commented out due to it being package-private

internal class JavaTestGetAll : AnnotationSpec() {
    internal interface TestCase {
        fun init(tx: Transaction, value: Double)

        fun branch1(tx: Transaction, value: Double)

        fun branch2(tx: Transaction, value: Double)

        fun merge(tx: Transaction, value: Double)

//        fun getConflicts(doc: Read): Conflicts

//        fun getConflictsAt(doc: Read, heads: Array<ChangeHash?>?): Conflicts
    }

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testGetAllInMap() {
        runMap { doc: Document -> doc.startTransaction() }
    }

    @Test
    fun testGetAllInList() {
        runList { doc: Document -> doc.startTransaction() }
    }

    fun runMap(createTx: Function<Document, Transaction>) {
        run(object : TestCase {
            override fun init(tx: Transaction, value: Double) {
                tx[ObjectId.ROOT, "key"] = value
            }

            override fun branch1(tx: Transaction, value: Double) {
                tx[ObjectId.ROOT, "key"] = value
            }

            override fun branch2(tx: Transaction, value: Double) {
                tx[ObjectId.ROOT, "key"] = value
            }

            override fun merge(tx: Transaction, value: Double) {
                tx[ObjectId.ROOT, "key"] = value
            }

//            override fun getConflicts(doc: Read): Conflicts {
//                return doc.getAll(ObjectId.ROOT, "key").get()
//            }

//            override fun getConflictsAt(doc: Read, heads: Array<ChangeHash?>?): Conflicts {
//                return doc.getAll(ObjectId.ROOT, "key", heads).get()
//            }
        }, createTx)
    }

    fun runList(createTx: Function<Document, Transaction>) {
        run(object : TestCase {
            var list: ObjectId? = null

            override fun init(tx: Transaction, value: Double) {
                list = tx.set(ObjectId.ROOT, "list", ObjectType.LIST)
                tx.insert(list, 0, value)
            }

            override fun branch1(tx: Transaction, value: Double) {
                tx[list, 0] = value
            }

            override fun branch2(tx: Transaction, value: Double) {
                tx[list, 0] = value
            }

            override fun merge(tx: Transaction, value: Double) {
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

    fun run(testCase: TestCase, createTx: Function<Document, Transaction>) {
        val doc = Document()
        createTx.apply(doc).use { tx ->
            testCase.init(tx, 1.23)
            tx.commit()
        }
        val doc2 = doc.fork()
        createTx.apply(doc).use { tx ->
            testCase.branch1(tx, 4.56)
            tx.commit()
        }
        createTx.apply(doc2).use { tx ->
            testCase.branch2(tx, 7.89)
            tx.commit()
        }
        doc.merge(doc2)
//        val heads = doc.heads
        // Check it works with an open transaction
        var tx = createTx.apply(doc)
//        var conflicts = testCase.getConflicts(doc)
//        assertConflicts(conflicts)

        // check the version from the transaction works
//        conflicts = testCase.getConflicts(tx)
//        assertConflicts(conflicts)

        // Check works without an open transaction
        tx.commit()
//        conflicts = testCase.getConflicts(doc)
//        assertConflicts(conflicts)

        createTx.apply(doc).use { tx2 ->
            testCase.merge(tx2, 2.00)
            tx2.commit()
        }
        // Check there are no conflicts after the merge
//        conflicts = testCase.getConflicts(doc)
//        Assertions.assertEquals(1, conflicts.values().size)

        // Check getAllAt works with an open transaction
        tx = createTx.apply(doc)
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
