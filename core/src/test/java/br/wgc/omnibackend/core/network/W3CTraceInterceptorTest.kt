package br.wgc.omnibackend.core.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class W3CTraceInterceptorTest {

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
    fun `intercept injects X-Request-ID and traceparent headers`() {
        val interceptor = W3CTraceInterceptor()
        val request = Request.Builder()
            .url("https://api.empresa.com/v1/resource")
            .get()
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        val requestId = captured.header(W3CTraceInterceptor.HEADER_REQUEST_ID)
        val traceparent = captured.header(W3CTraceInterceptor.HEADER_TRACEPARENT)

        assertNotNull(requestId)
        assertNotNull(traceparent)
        // W3C traceparent format: 00-{32 hex trace-id}-{16 hex parent-id}-01
        assertTrue(traceparent!!.matches(Regex("^00-[a-f0-9]{32}-[a-f0-9]{16}-01$")))
    }

    @Test
    fun `intercept preserves existing traceparent and requestId`() {
        val existingRequestId = "trace-req-999"
        val existingTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"

        val interceptor = W3CTraceInterceptor()
        val request = Request.Builder()
            .url("https://api.empresa.com/v1/resource")
            .header(W3CTraceInterceptor.HEADER_REQUEST_ID, existingRequestId)
            .header(W3CTraceInterceptor.HEADER_TRACEPARENT, existingTraceparent)
            .get()
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals(existingRequestId, captured.header(W3CTraceInterceptor.HEADER_REQUEST_ID))
        assertEquals(existingTraceparent, captured.header(W3CTraceInterceptor.HEADER_TRACEPARENT))
    }
}
