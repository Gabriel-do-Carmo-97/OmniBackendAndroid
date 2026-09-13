package br.wgc.omnibackend.back4app

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class OmniBack4AppTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "parse_test_dir")
        tempDir.mkdirs()
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.getDir(any(), any()) } returns tempDir
        every { mockContext.cacheDir } returns tempDir
        every { mockContext.filesDir } returns tempDir
        OmniBack4App.resetForTesting()
    }

    @Test
    fun `initialize configures appId, clientKey and serverUrl correctly`() {
        val appId = "test-app-id"
        val clientKey = "test-client-key"
        val serverUrl = "https://parseapi.back4app.com"

        OmniBack4App.initialize(
            context = mockContext,
            appId = appId,
            clientKey = clientKey,
            serverUrl = serverUrl,
        )

        assertTrue(OmniBack4App.initialized)
        assertEquals(appId, OmniBack4App.appId)
        assertEquals(clientKey, OmniBack4App.clientKey)
        assertEquals(serverUrl, OmniBack4App.serverUrl)
    }
}
