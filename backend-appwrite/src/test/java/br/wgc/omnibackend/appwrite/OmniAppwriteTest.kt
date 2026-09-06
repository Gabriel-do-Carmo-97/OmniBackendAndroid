package br.wgc.omnibackend.appwrite

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OmniAppwriteTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockContext.applicationContext } returns mockContext
        OmniAppwrite.resetForTesting()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        OmniAppwrite.resetForTesting()
    }

    @Test
    fun `initialize configures endpoint, projectId, databaseId and bucketId correctly`() {
        val testEndpoint = "https://cloud.appwrite.io/v1"
        val testProjectId = "test-project-123"
        val testDatabaseId = "test-db-456"
        val testBucketId = "test-bucket-789"

        OmniAppwrite.initialize(
            context = mockContext,
            endpoint = testEndpoint,
            projectId = testProjectId,
            databaseId = testDatabaseId,
            defaultBucketId = testBucketId
        )

        assertTrue(OmniAppwrite.initialized)
        assertEquals(testEndpoint, OmniAppwrite.endpoint)
        assertEquals(testProjectId, OmniAppwrite.projectId)
        assertEquals(testDatabaseId, OmniAppwrite.databaseId)
        assertNotNull(OmniAppwrite.auth)
        assertNotNull(OmniAppwrite.database)
        assertNotNull(OmniAppwrite.storage)
    }
}
