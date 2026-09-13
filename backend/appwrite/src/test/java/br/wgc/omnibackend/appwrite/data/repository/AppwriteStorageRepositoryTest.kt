package br.wgc.omnibackend.appwrite.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.File
import io.appwrite.services.Storage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppwriteStorageRepositoryTest {

    private val mockStorage: Storage = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)
    private lateinit var storageRepository: AppwriteStorageRepositoryImpl

    private val endpoint = "https://cloud.appwrite.io/v1"
    private val projectId = "test-project"
    private val defaultBucket = "default-bucket"

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val urlStr = firstArg<String>()
            val mockUri = mockk<Uri>()
            every { mockUri.toString() } returns urlStr
            mockUri
        }

        storageRepository = AppwriteStorageRepositoryImpl(
            storage = mockStorage,
            context = mockContext,
            endpoint = endpoint,
            projectId = projectId,
            defaultBucketId = defaultBucket,
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `uploadFileDirect returns Success with public view URL`() = runTest {
        val bytes = "hello world".toByteArray()
        val mockFile = mockk<File>(relaxed = true)
        coEvery { mockStorage.createFile("my-bucket", "file-123", any()) } returns mockFile

        val result = storageRepository.uploadFileDirect("my-bucket/file-123", bytes)

        assertTrue(result is DataResult.Success)
        val uri = (result as DataResult.Success).data
        val expectedUrl = "$endpoint/storage/buckets/my-bucket/files/file-123/view?project=$projectId"
        assertEquals(expectedUrl, uri.toString())
    }

    @Test
    fun `delete returns Success when file deleted successfully`() = runTest {
        coEvery { mockStorage.deleteFile("my-bucket", "file-123") } returns Any()

        val result = storageRepository.delete("my-bucket/file-123")

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `getDownloadUrl returns Failure ObjectNotFound on 404`() = runTest {
        coEvery { mockStorage.deleteFile("my-bucket", "missing") } throws
            AppwriteException("File not found", 404, "file_not_found")

        val result = storageRepository.delete("my-bucket/missing")

        assertTrue(result is DataResult.Failure)
        val error = (result as DataResult.Failure).error
        assertTrue(error is AppError.Storage.ObjectNotFound)
    }
}
