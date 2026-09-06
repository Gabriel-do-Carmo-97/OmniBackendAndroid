package br.wgc.omnibackend.core.featureflag

import br.wgc.omnibackend.core.repository.FeatureFlagRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HybridFeatureFlagRepositoryTest {

    private val mockRemoteRepo: FeatureFlagRepository = mockk(relaxed = true)
    private lateinit var hybridFlagRepo: HybridFeatureFlagRepository

    @Before
    fun setUp() {
        hybridFlagRepo = HybridFeatureFlagRepository(mockRemoteRepo)
    }

    @Test
    fun `getBoolean returns remote value when no override exists`() {
        every { mockRemoteRepo.getBoolean("new_ui_enabled", false) } returns true

        val result = hybridFlagRepo.getBoolean("new_ui_enabled", false)

        assertTrue(result)
    }

    @Test
    fun `getBoolean returns override value when override is set`() {
        every { mockRemoteRepo.getBoolean("new_ui_enabled", false) } returns false

        hybridFlagRepo.setOverride("new_ui_enabled", true)

        val result = hybridFlagRepo.getBoolean("new_ui_enabled", false)

        assertTrue(result)
    }

    @Test
    fun `clearOverride restores remote or default value`() {
        every { mockRemoteRepo.getBoolean("new_ui_enabled", false) } returns false

        hybridFlagRepo.setOverride("new_ui_enabled", true)
        hybridFlagRepo.clearOverride("new_ui_enabled")

        val result = hybridFlagRepo.getBoolean("new_ui_enabled", false)

        assertFalse(result)
    }
}
