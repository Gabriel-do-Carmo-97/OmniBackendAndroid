package br.wgc.omnibackend.cloudflare

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OmniCloudflareTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { mockContext.applicationContext } returns mockContext
        OmniCloudflare.resetForTesting()
    }

    @Test
    fun `initialize configures accountId and workerBaseUrl correctly`() {
        val accountId = "test-account-123"
        val workerUrl = "https://test.workers.dev"

        OmniCloudflare.initialize(
            context = mockContext,
            accountId = accountId,
            workerBaseUrl = workerUrl,
        )

        assertTrue(OmniCloudflare.initialized)
        assertEquals(accountId, OmniCloudflare.accountId)
        assertEquals(workerUrl, OmniCloudflare.workerBaseUrl)
        assertNotNull(OmniCloudflare.auth)
        assertNotNull(OmniCloudflare.database)
        assertNotNull(OmniCloudflare.storage)
    }
}
