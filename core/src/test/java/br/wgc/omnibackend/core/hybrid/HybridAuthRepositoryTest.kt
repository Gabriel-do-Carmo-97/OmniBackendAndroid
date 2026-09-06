package br.wgc.omnibackend.core.hybrid

import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HybridAuthRepositoryTest {

    private val mockPrimary: AuthRepository = mockk(relaxed = true)
    private val mockFallback: AuthRepository = mockk(relaxed = true)

    private lateinit var hybridAuthRepository: HybridAuthRepository

    @Before
    fun setUp() {
        hybridAuthRepository = HybridAuthRepository(mockPrimary, mockFallback)
    }

    @Test
    fun `login returns Success from primary when primary succeeds`() = runTest {
        val userPrimary = OmniUser(uid = "p_1", email = "p@test.com")
        coEvery { mockPrimary.login("p@test.com", "pass") } returns DataResult.Success(userPrimary)

        val result = hybridAuthRepository.login("p@test.com", "pass")

        assertTrue(result is DataResult.Success)
        assertEquals("p_1", (result as DataResult.Success).data.uid)
    }

    @Test
    fun `login falls back to secondary when primary fails`() = runTest {
        val userFallback = OmniUser(uid = "f_2", email = "f@test.com")
        coEvery { mockPrimary.login("f@test.com", "pass") } returns DataResult.Failure(AppError.Generic.Network)
        coEvery { mockFallback.login("f@test.com", "pass") } returns DataResult.Success(userFallback)

        val result = hybridAuthRepository.login("f@test.com", "pass")

        assertTrue(result is DataResult.Success)
        assertEquals("f_2", (result as DataResult.Success).data.uid)
    }
}
