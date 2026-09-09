package br.wgc.omnibackend.supabase

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OmniSupabaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockContext.applicationContext } returns mockContext
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize configures url and anonKey correctly`() {
        val testUrl = "https://xyzcompany.supabase.co"
        val testKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testkey"

        OmniSupabase.initialize(
            context = mockContext,
            url = testUrl,
            anonKey = testKey,
            sessionManager = io.github.jan.supabase.auth.MemorySessionManager(),
            codeVerifierCache = io.github.jan.supabase.auth.MemoryCodeVerifierCache()
        )

        assertTrue(OmniSupabase.initialized)
        assertEquals(testUrl, OmniSupabase.url)
        assertEquals(testKey, OmniSupabase.anonKey)
        assertNotNull(OmniSupabase.auth)
        assertNotNull(OmniSupabase.database)
        assertNotNull(OmniSupabase.storage)
    }
}
