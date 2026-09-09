package br.wgc.omnibackend.amplify

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OmniAmplifyTest {

    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { mockContext.applicationContext } returns mockContext
        OmniAmplify.resetForTesting()
    }

    @Test
    fun `initialize configures Amplify correctly`() {
        OmniAmplify.initialize(mockContext)

        assertTrue(OmniAmplify.initialized)
        assertNotNull(OmniAmplify.auth)
        assertNotNull(OmniAmplify.database)
        assertNotNull(OmniAmplify.storage)
    }
}
