package br.wgc.omnibackend.firebase.domain.usecase

import br.wgc.omnibackend.firebase.domain.repository.AuthRepository
import br.wgc.omnibackend.firebase.domain.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.PresenceRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val databaseRepository: RealtimeDatabaseRepository = mockk()
    private val presenceRepository: PresenceRepository = mockk()

    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        coEvery { databaseRepository.presence() } returns presenceRepository
        loginUseCase = LoginUseCase(authRepository, databaseRepository)
    }

    @Test
    fun `invoke emit Loading e Success quando auth e bem sucedida sem atualizar presenca`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = "user_123"

        coEvery { authRepository.loginEmailWithPassword(email, password) } returns DataResult.Success(userId)

        val results = loginUseCase(email, password, updatePresence = false).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UseCaseResult.Loading)
        assertTrue(results[1] is UseCaseResult.Success)
        assertEquals(userId, (results[1] as UseCaseResult.Success).data)
        coVerify(exactly = 0) { presenceRepository.goOnline(any(), any()) }
    }

    @Test
    fun `invoke emit Loading e Success quando auth e presenca sao bem sucedidas`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = "user_123"

        coEvery { authRepository.loginEmailWithPassword(email, password) } returns DataResult.Success(userId)
        coEvery { presenceRepository.goOnline("users", userId) } returns DataResult.Success(Unit)

        val results = loginUseCase(email, password, updatePresence = true).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UseCaseResult.Loading)
        assertTrue(results[1] is UseCaseResult.Success)
        assertEquals(userId, (results[1] as UseCaseResult.Success).data)
    }

    @Test
    fun `invoke desloga e emite Failure quando updatePresence e true mas presenca falha`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val userId = "user_123"
        val error = AppError.RealtimeDatabase.PermissionDenied

        coEvery { authRepository.loginEmailWithPassword(email, password) } returns DataResult.Success(userId)
        coEvery { presenceRepository.goOnline("users", userId) } returns DataResult.Failure(error)
        coEvery { authRepository.signOut() } returns DataResult.Success(Unit)

        val results = loginUseCase(email, password, updatePresence = true).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UseCaseResult.Loading)
        assertTrue(results[1] is UseCaseResult.Failure)
        assertEquals(error, (results[1] as UseCaseResult.Failure).error)
        coVerify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `invoke emit Loading e Failure quando auth falha`() = runTest {
        val email = "test@example.com"
        val password = "wrong_password"
        val error = AppError.Auth.InvalidCredentials

        coEvery { authRepository.loginEmailWithPassword(email, password) } returns DataResult.Failure(error)

        val results = loginUseCase(email, password).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UseCaseResult.Loading)
        assertTrue(results[1] is UseCaseResult.Failure)
        assertEquals(error, (results[1] as UseCaseResult.Failure).error)
    }

    @Test
    fun `loginAnonymous emit Loading e Success quando login e bem sucedido sem presenca`() = runTest {
        val userId = "anon_123"

        coEvery { authRepository.loginAnonymously() } returns DataResult.Success(userId)

        val results = loginUseCase.loginAnonymous(updatePresence = false).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is UseCaseResult.Loading)
        assertTrue(results[1] is UseCaseResult.Success)
        assertEquals(userId, (results[1] as UseCaseResult.Success).data)
        coVerify(exactly = 0) { presenceRepository.goOnline(any(), any()) }
    }
}

