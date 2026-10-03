package br.wgc.omnibackend.core.network

import io.mockk.mockk
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniHttpClientFactoryTest {

    @Test
    fun `createClient returns OkHttpClient with default timeouts and interceptors`() {
        val client = OmniHttpClientFactory.createClient()

        assertNotNull(client)
        assertEquals(15000, client.connectTimeoutMillis)
        assertEquals(30000, client.readTimeoutMillis)
        assertEquals(30000, client.writeTimeoutMillis)

        val interceptors = client.interceptors
        assertTrue(interceptors.any { it is IdempotencyInterceptor })
        assertTrue(interceptors.any { it is W3CTraceInterceptor })
    }

    @Test
    fun `createClient with tokenProvider attaches AuthBearerInterceptor and TokenAuthenticator`() {
        val tokenProvider: TokenProvider = mockk()
        val client = OmniHttpClientFactory.createClient(tokenProvider = tokenProvider)

        val interceptors = client.interceptors
        assertTrue(interceptors.any { it is AuthBearerInterceptor })
        assertTrue(client.authenticator is TokenAuthenticator)
    }

    @Test
    fun `createClient with enableLogging attaches HttpLoggingInterceptor`() {
        val client = OmniHttpClientFactory.createClient(enableLogging = true)

        val interceptors = client.interceptors
        assertTrue(interceptors.any { it is HttpLoggingInterceptor })
    }

    @Test
    fun `createClient custom timeouts are respected`() {
        val client = OmniHttpClientFactory.createClient(
            connectTimeoutSeconds = 5,
            readTimeoutSeconds = 10,
            writeTimeoutSeconds = 12,
        )

        assertEquals(5000, client.connectTimeoutMillis)
        assertEquals(10000, client.readTimeoutMillis)
        assertEquals(12000, client.writeTimeoutMillis)
    }
}
