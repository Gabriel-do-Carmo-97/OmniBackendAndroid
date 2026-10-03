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

class CloudflareAuthRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: CloudflareAuthRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val workerBaseUrl = mockWebServer.url("").toString().removeSuffix("/")
        val client = OmniHttpClientFactory.createClient()
        repository = CloudflareAuthRepositoryImpl(workerBaseUrl = workerBaseUrl, httpClient = client)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `login sends POST to worker and receives user successfully with enterprise headers`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"cf-user-456","email":"edge@cloudflare.com","name":"Cloudflare User"}"""),
        )

        val result = repository.login("edge@cloudflare.com", "workerPass123")

        assertTrue(result is DataResult.Success)
        val user = (result as DataResult.Success).data
        assertEquals("cf-user-456", user.uid)
        assertEquals("edge@cloudflare.com", user.email)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/auth/login", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertNotNull(recordedRequest.getHeader(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_TRACEPARENT))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_REQUEST_ID))
    }

    @Test
    fun `loginAnonymously receives anonymous uid`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"anon-cf-789"}"""),
        )

        val result = repository.loginAnonymously()
        assertTrue(result is DataResult.Success)
        assertEquals("anon-cf-789", (result as DataResult.Success).data)
    }
}
