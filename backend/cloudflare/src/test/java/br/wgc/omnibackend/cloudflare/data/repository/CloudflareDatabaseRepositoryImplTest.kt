package br.wgc.omnibackend.cloudflare.data.repository

import br.wgc.omnibackend.core.network.IdempotencyInterceptor
import br.wgc.omnibackend.core.network.OmniHttpClientFactory
import br.wgc.omnibackend.core.network.W3CTraceInterceptor
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class CfDocument(val id: String = "", val title: String = "")

class CloudflareDatabaseRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: CloudflareDatabaseRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val workerBaseUrl = mockWebServer.url("").toString().removeSuffix("/")
        val client = OmniHttpClientFactory.createClient()
        repository = CloudflareDatabaseRepositoryImpl(workerBaseUrl = workerBaseUrl, httpClient = client)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `addDocument sends POST to d1 endpoint and receives id`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"d1-doc-123","title":"Edge Document"}"""),
        )

        val result = repository.addDocument("posts", CfDocument(title = "Edge Document"))

        assertTrue(result is DataResult.Success)
        assertEquals("d1-doc-123", (result as DataResult.Success).data)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/d1/posts", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertNotNull(recordedRequest.getHeader(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_TRACEPARENT))
    }

    @Test
    fun `getDocument sends GET to d1 endpoint and deserializes model`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"d1-doc-123","title":"Edge Document"}"""),
        )

        val result = repository.getDocument("posts", "d1-doc-123", CfDocument::class.java)

        assertTrue(result is DataResult.Success)
        val doc = (result as DataResult.Success).data
        assertNotNull(doc)
        assertEquals("d1-doc-123", doc?.id)
        assertEquals("Edge Document", doc?.title)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/d1/posts/d1-doc-123", recordedRequest.path)
        assertEquals("GET", recordedRequest.method)
    }

    @Test
    fun `deleteDocument sends DELETE to d1 endpoint successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        val result = repository.deleteDocument("posts", "d1-doc-123")
        assertTrue(result is DataResult.Success)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/d1/posts/d1-doc-123", recordedRequest.path)
        assertEquals("DELETE", recordedRequest.method)
    }
}
