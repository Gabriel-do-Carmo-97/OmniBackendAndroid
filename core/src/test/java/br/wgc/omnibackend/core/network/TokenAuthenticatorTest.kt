package br.wgc.omnibackend.core.network

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {

    private val tokenProvider: TokenProvider = mockk(relaxed = true)
    private val authenticator = TokenAuthenticator(tokenProvider)

    @Test
    fun `authenticate returns new request with refreshed token on 401 response`() {
        val oldToken = "expired-token"
        val newToken = "fresh-jwt-token"

        val request = Request.Builder()
            .url("https://api.empresa.com/v1/protected")
            .header("Authorization", "Bearer $oldToken")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { tokenProvider.getAccessToken() } returns oldToken
        coEvery { tokenProvider.refreshToken() } returns newToken

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNotNull(authenticatedRequest)
        assertEquals("Bearer $newToken", authenticatedRequest?.header("Authorization"))
    }

    @Test
    fun `authenticate reuses already refreshed token if another coroutine updated it`() {
        val oldToken = "expired-token"
        val concurrentToken = "concurrently-refreshed-token"

        val request = Request.Builder()
            .url("https://api.empresa.com/v1/protected")
            .header("Authorization", "Bearer $oldToken")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        // getAccessToken already returns the new token!
        coEvery { tokenProvider.getAccessToken() } returns concurrentToken

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNotNull(authenticatedRequest)
        assertEquals("Bearer $concurrentToken", authenticatedRequest?.header("Authorization"))
        coVerify(exactly = 0) { tokenProvider.refreshToken() }
    }

    @Test
    fun `authenticate notifies onSessionExpired and returns null when refresh fails`() {
        val oldToken = "expired-token"

        val request = Request.Builder()
            .url("https://api.empresa.com/v1/protected")
            .header("Authorization", "Bearer $oldToken")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { tokenProvider.getAccessToken() } returns oldToken
        coEvery { tokenProvider.refreshToken() } returns null

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNull(authenticatedRequest)
        verify(exactly = 1) { tokenProvider.onSessionExpired() }
    }

    @Test
    fun `authenticate aborts after max retry attempts`() {
        val request = Request.Builder()
            .url("https://api.empresa.com/v1/protected")
            .header("Authorization", "Bearer token-3")
            .build()

        val response1 = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val response2 = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(response1)
            .build()

        val response3 = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(response2)
            .build()

        val authenticatedRequest = authenticator.authenticate(null, response3)
        assertNull(authenticatedRequest)
    }
}
