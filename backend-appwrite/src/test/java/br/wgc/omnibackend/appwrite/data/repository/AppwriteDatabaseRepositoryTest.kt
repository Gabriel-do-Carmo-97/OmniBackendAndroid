package br.wgc.omnibackend.appwrite.data.repository

import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import com.google.gson.Gson
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class TestEntity(val name: String, val score: Int)

@OptIn(ExperimentalCoroutinesApi::class)
class AppwriteDatabaseRepositoryTest {

    private val mockDatabases: Databases = mockk(relaxed = true)
    private val mockRealtime: Realtime = mockk(relaxed = true)
    private val databaseId = "test-db"
    private val gson = Gson()

    private lateinit var databaseRepository: AppwriteDatabaseRepositoryImpl

    @Before
    fun setUp() {
        databaseRepository = AppwriteDatabaseRepositoryImpl(
            databases = mockDatabases,
            realtime = mockRealtime,
            databaseId = databaseId,
            gson = gson
        )
    }

    @Test
    fun `addDocument returns Success with document ID`() = runTest {
        val testObj = TestEntity("Gabriel", 100)
        val mockDocument = mockk<Document<Map<String, Any>>>(relaxed = true)
        coEvery { mockDocument.id } returns "doc_999"

        coEvery {
            mockDatabases.createDocument(
                databaseId = databaseId,
                collectionId = "users",
                documentId = any(),
                data = any()
            )
        } returns mockDocument

        val result = databaseRepository.addDocument("users", testObj)

        assertTrue(result is DataResult.Success)
        val id = (result as DataResult.Success).data
        assertEquals("doc_999", id)
    }

    @Test
    fun `getDocument returns Success with mapped object`() = runTest {
        val mockDocument = mockk<Document<Map<String, Any>>>(relaxed = true)
        val dataMap = mapOf<String, Any>("name" to "Gabriel", "score" to 100.0)
        coEvery { mockDocument.data } returns dataMap

        coEvery {
            mockDatabases.getDocument(databaseId, "users", "doc_999")
        } returns mockDocument

        val result = databaseRepository.getDocument("users", "doc_999", TestEntity::class.java)

        assertTrue(result is DataResult.Success)
        val entity = (result as DataResult.Success).data
        assertEquals("Gabriel", entity?.name)
        assertEquals(100, entity?.score)
    }

    @Test
    fun `getDocument returns Failure DocumentNotFound on 404`() = runTest {
        coEvery {
            mockDatabases.getDocument(databaseId, "users", "missing_doc")
        } throws AppwriteException("Document not found", 404, "document_not_found")

        val result = databaseRepository.getDocument("users", "missing_doc", TestEntity::class.java)

        assertTrue(result is DataResult.Failure)
        val error = (result as DataResult.Failure).error
        assertTrue(error is AppError.Firestore.DocumentNotFound)
    }

    @Test
    fun `deleteDocument returns Success`() = runTest {
        coEvery {
            mockDatabases.deleteDocument(databaseId, "users", "doc_999")
        } returns Any()

        val result = databaseRepository.deleteDocument("users", "doc_999")

        assertTrue(result is DataResult.Success)
    }
}
