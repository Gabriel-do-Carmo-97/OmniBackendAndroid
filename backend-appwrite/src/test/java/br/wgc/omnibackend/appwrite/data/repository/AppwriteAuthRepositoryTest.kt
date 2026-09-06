package br.wgc.omnibackend.appwrite.data.repository

import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppwriteAuthRepositoryTest {

    private val mockAccount: Account = mockk(relaxed = true)
    private lateinit var authRepository: AppwriteAuthRepositoryImpl

    @Before
    fun setUp() {
        authRepository = AppwriteAuthRepositoryImpl(mockAccount)
    }

    @Test
    fun `login returns Success with OmniUser on valid credentials`() = runTest {
        val mockSession = mockk<Session>(relaxed = true)
        val mockUser = mockk<User<Map<String, Any>>>(relaxed = true)
        coEvery { mockSession.id } returns "session_123"
        coEvery { mockUser.id } returns "user_123"
        coEvery { mockUser.email } returns "user@example.com"
        coEvery { mockUser.name } returns "Test User"
        coEvery { mockUser.emailVerification } returns true

        coEvery { mockAccount.createEmailPasswordSession("user@example.com", "password123") } returns mockSession
        coEvery { mockAccount.get() } returns mockUser

        val result = authRepository.login("user@example.com", "password123")

        assertTrue(result is DataResult.Success)
        val user = (result as DataResult.Success).data
        assertEquals("user_123", user.uid)
        assertEquals("user@example.com", user.email)
        assertEquals("Test User", user.displayName)
    }

    @Test
    fun `login returns Failure InvalidCredentials on AppwriteException 401`() = runTest {
        coEvery { mockAccount.createEmailPasswordSession("user@example.com", "wrong") } throws
            AppwriteException("Unauthorized", 401, "user_unauthorized")

        val result = authRepository.login("user@example.com", "wrong")

        assertTrue(result is DataResult.Failure)
        val error = (result as DataResult.Failure).error
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `signOut calls deleteSession and returns Success`() = runTest {
        val mockSession = mockk<Session>(relaxed = true)
        coEvery { mockAccount.deleteSession("current") } returns mockSession

        val result = authRepository.signOut()

        assertTrue(result is DataResult.Success)
        coVerify { mockAccount.deleteSession("current") }
    }
}
