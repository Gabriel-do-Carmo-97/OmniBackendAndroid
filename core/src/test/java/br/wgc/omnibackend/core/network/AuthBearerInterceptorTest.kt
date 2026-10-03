package br.wgc.omnibackend.core.network

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthBearerInterceptorTest {

    private val chain: Interceptor.Chain = mockk()
    private val tokenProvider: TokenProvider = mockk()
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
    fun `intercept injects Bearer token from cache when authorization header is absent`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.cached-token"
        every { tokenProvider.getCachedAccessToken() } returns token

        val interceptor = AuthBearerInterceptor(tokenProvider)
        val request = Request.Builder().url("https://api.empresa.com/v1/profile").get().build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals("Bearer $token", captured.header("Authorization"))
    }

    @Test
    fun `intercept injects Bearer token from getAccessToken when cached is null`() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fetched-token"
        every { tokenProvider.getCachedAccessToken() } returns null
        coEvery { tokenProvider.getAccessToken() } returns token

        val interceptor = AuthBearerInterceptor(tokenProvider)
        val request = Request.Builder().url("https://api.empresa.com/v1/profile").get().build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals("Bearer $token", captured.header("Authorization"))
    }

    @Test
    fun `intercept preserves existing Authorization header`() {
        val existingHeader = "Bearer manual-token-123"
        every { tokenProvider.getCachedAccessToken() } returns "other-token"

        val interceptor = AuthBearerInterceptor(tokenProvider)
        val request = Request.Builder()
            .url("https://api.empresa.com/v1/profile")
            .header("Authorization", existingHeader)
            .get()
            .build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertEquals(existingHeader, captured.header("Authorization"))
    }

    @Test
    fun `intercept leaves request untouched when token is null`() {
        every { tokenProvider.getCachedAccessToken() } returns null
        coEvery { tokenProvider.getAccessToken() } returns null

        val interceptor = AuthBearerInterceptor(tokenProvider)
        val request = Request.Builder().url("https://api.empresa.com/v1/profile").get().build()
        every { chain.request() } returns request

        interceptor.intercept(chain)

        val captured = requestSlot.captured
        assertNull(captured.header("Authorization"))
    }
}
