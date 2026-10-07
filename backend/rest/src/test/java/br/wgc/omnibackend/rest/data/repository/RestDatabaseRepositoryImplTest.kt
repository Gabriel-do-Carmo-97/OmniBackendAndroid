package br.wgc.omnibackend.rest.data.repository

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

data class TestDocument(val id: String = "", val name: String = "")

class RestDatabaseRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: RestDatabaseRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val baseUrl = mockWebServer.url("").toString().removeSuffix("/")
        val client = OmniHttpClientFactory.createClient()
        repository = RestDatabaseRepositoryImpl(baseUrl = baseUrl, httpClient = client)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `addDocument sends POST with body and receives id with enterprise headers`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"doc-999","name":"Sample"}"""),
        )

        val result = repository.addDocument("items", TestDocument(name = "Sample"))

        assertTrue(result is DataResult.Success)
        assertEquals("doc-999", (result as DataResult.Success).data)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/items", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertNotNull(recordedRequest.getHeader(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_TRACEPARENT))
    }

    @Test
    fun `getDocument sends GET and deserializes model`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"doc-999","name":"Sample"}"""),
        )

        val result = repository.getDocument("items", "doc-999", TestDocument::class.java)

        assertTrue(result is DataResult.Success)
        val doc = (result as DataResult.Success).data
        assertNotNull(doc)
        assertEquals("doc-999", doc?.id)
        assertEquals("Sample", doc?.name)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/items/doc-999", recordedRequest.path)
        assertEquals("GET", recordedRequest.method)
    }

    @Test
    fun `deleteDocument sends DELETE successfully`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        val result = repository.deleteDocument("items", "doc-999")
        assertTrue(result is DataResult.Success)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/items/doc-999", recordedRequest.path)
        assertEquals("DELETE", recordedRequest.method)
    }
}
