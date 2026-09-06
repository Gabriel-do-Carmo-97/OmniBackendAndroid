package br.wgc.omnibackend.pocketbase

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OmniPocketBaseTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { mockContext.applicationContext } returns mockContext
        OmniPocketBase.resetForTesting()
    }

    @Test
    fun `initialize configures baseUrl correctly`() {
        val testUrl = "https://example.pocketbase.io"

        OmniPocketBase.initialize(
            context = mockContext,
            baseUrl = testUrl
        )

        assertTrue(OmniPocketBase.initialized)
        assertEquals(testUrl, OmniPocketBase.baseUrl)
        assertNotNull(OmniPocketBase.auth)
        assertNotNull(OmniPocketBase.database)
        assertNotNull(OmniPocketBase.storage)
    }
}
