package br.wgc.omnibackend.rest

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OmniRestBackendTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { mockContext.applicationContext } returns mockContext
        OmniRestBackend.resetForTesting()
    }

    @Test
    fun `initialize configures baseUrl correctly`() {
        val testUrl = "https://api.empresa.com"

        OmniRestBackend.initialize(
            context = mockContext,
            baseUrl = testUrl,
        )

        assertTrue(OmniRestBackend.initialized)
        assertEquals(testUrl, OmniRestBackend.baseUrl)
        assertNotNull(OmniRestBackend.auth)
        assertNotNull(OmniRestBackend.database)
        assertNotNull(OmniRestBackend.storage)
    }
}
