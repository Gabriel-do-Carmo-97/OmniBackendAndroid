package br.wgc.omnibackend.amplify.data.repository

import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AmplifyDatabaseRepositoryTest {

    private lateinit var repository: AmplifyDatabaseRepositoryImpl

    data class TestModel(val id: String = "", val name: String = "", val count: Int = 0)

    @Before
    fun setUp() {
        repository = AmplifyDatabaseRepositoryImpl()
    }

    @Test
    fun `addDocument and getDocument work correctly`() = runTest {
        val item = TestModel(id = "doc1", name = "AmplifyItem", count = 10)
        val addResult = repository.addDocument("items", item, customId = "doc1")
        assertTrue(addResult is DataResult.Success)

        val getResult = repository.getDocument("items", "doc1", TestModel::class.java)
        assertTrue(getResult is DataResult.Success)
        val retrieved = (getResult as DataResult.Success).data
        assertNotNull(retrieved)
        assertEquals("AmplifyItem", retrieved?.name)
        assertEquals(10, retrieved?.count)
    }

    @Test
    fun `updateDocument and deleteDocument work correctly`() = runTest {
        val item = TestModel(id = "doc2", name = "Original", count = 5)
        repository.addDocument("items", item, customId = "doc2")

        val updateResult = repository.updateDocument("items", "doc2", mapOf("name" to "Updated"))
        assertTrue(updateResult is DataResult.Success)

        val getResult = repository.getDocument("items", "doc2", TestModel::class.java)
        assertEquals("Updated", (getResult as DataResult.Success).data?.name)

        val deleteResult = repository.deleteDocument("items", "doc2")
        assertTrue(deleteResult is DataResult.Success)

        val afterDelete = repository.getDocument("items", "doc2", TestModel::class.java)
        assertTrue(afterDelete is DataResult.Success)
        assertEquals(null, (afterDelete as DataResult.Success).data)
    }

    @Test
    fun `findDocuments returns stored items`() = runTest {
        val item1 = TestModel(id = "docA", name = "A", count = 1)
        val item2 = TestModel(id = "docB", name = "B", count = 2)
        repository.addDocument("items", item1, customId = "docA")
        repository.addDocument("items", item2, customId = "docB")

        val listResult = repository.findDocuments("items", emptyList(), TestModel::class.java)
        assertTrue(listResult is DataResult.Success)
        val list = (listResult as DataResult.Success).data
        assertEquals(2, list.size)
    }
}
