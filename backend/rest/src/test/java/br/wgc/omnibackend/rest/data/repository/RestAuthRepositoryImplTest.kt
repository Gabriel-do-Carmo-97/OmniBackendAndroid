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

class RestAuthRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: RestAuthRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val baseUrl = mockWebServer.url("").toString().removeSuffix("/")
        val client = OmniHttpClientFactory.createClient()
        repository = RestAuthRepositoryImpl(baseUrl = baseUrl, httpClient = client)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `login sends POST with json and receives user successfully with enterprise headers`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"user-123","email":"dev@empresa.com","displayName":"Developer"}"""),
        )

        val result = repository.login("dev@empresa.com", "secret123")

        assertTrue(result is DataResult.Success)
        val user = (result as DataResult.Success).data
        assertEquals("user-123", user.uid)
        assertEquals("dev@empresa.com", user.email)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/auth/login", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        assertNotNull(recordedRequest.getHeader(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_TRACEPARENT))
        assertNotNull(recordedRequest.getHeader(W3CTraceInterceptor.HEADER_REQUEST_ID))
    }

    @Test
    fun `resetPassword executes successfully on 200 response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val result = repository.resetPassword("dev@empresa.com")
        assertTrue(result is DataResult.Success)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/auth/reset-password", recordedRequest.path)
    }
}
