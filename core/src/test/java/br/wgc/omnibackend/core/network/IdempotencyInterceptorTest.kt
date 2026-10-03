package br.wgc.omnibackend.core.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IdempotencyInterceptorTest {

    private val chain: Interceptor.Chain = mockk()
    private val requestSlot = slot<Request>()

    init {
        every { chain.proceed(capture(requestSlot)) } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }
    }

    @Test
    fun `intercept adds idempotency header to POST requests when absent`() {
        val interceptor = IdempotencyInterceptor()
        val request = Request.Builder()
            .url("https://api.empresa.com/orders")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val interceptedRequest = requestSlot.captured
        val headerValue = interceptedRequest.header(IdempotencyInterceptor.DEFAULT_HEADER_NAME)
        assertNotNull(headerValue)
    }

    @Test
    fun `intercept does not modify GET requests`() {
        val interceptor = IdempotencyInterceptor()
        val request = Request.Builder()
            .url("https://api.empresa.com/orders")
            .get()
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val interceptedRequest = requestSlot.captured
        assertNull(interceptedRequest.header(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
    }

    @Test
    fun `intercept preserves existing idempotency header`() {
        val customKey = "my-custom-uuid-1234"
        val interceptor = IdempotencyInterceptor()
        val request = Request.Builder()
            .url("https://api.empresa.com/orders")
            .header(IdempotencyInterceptor.DEFAULT_HEADER_NAME, customKey)
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val interceptedRequest = requestSlot.captured
        assertEquals(customKey, interceptedRequest.header(IdempotencyInterceptor.DEFAULT_HEADER_NAME))
    }
}
