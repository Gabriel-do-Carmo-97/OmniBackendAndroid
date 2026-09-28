package br.wgc.omnibackendandroid

import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.testing.FakeAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sample App Integration & Consumer Test.
 *
 * Validates that client applications consume OmniBackend contracts,
 * fakes, and reactive state flows cleanly and deterministically.
 */
class SampleAppIntegrationTest {

    @Test
    fun `sample app should authenticate and receive user state from fake repository`() = runTest {
        // Given
        val initialUser = OmniUser(uid = "usr-001", email = "developer@empresa.com.br")
        val fakeAuth = FakeAuthRepository(initialUser = initialUser)

        // When
        val currentUser = fakeAuth.currentUser
        val currentFlowUser = fakeAuth.authState.first()

        // Then
        assertEquals(initialUser, currentUser)
        assertEquals("developer@empresa.com.br", currentFlowUser?.email)
    }

    @Test
    fun `sample app should handle successful login and user registration flows`() = runTest {
        // Given
        val fakeAuth = FakeAuthRepository()
        val email = "newuser@empresa.com.br"
        val password = "securePassword123"

        // When creating a user
        val createResult = fakeAuth.createUser(email, password)
        assertTrue("User creation should succeed", createResult is DataResult.Success<*>)

        // When logging in
        val loginResult = fakeAuth.login(email, password)
        assertTrue("Login should succeed", loginResult is DataResult.Success<*>)

        val loggedInUser = (loginResult as DataResult.Success<OmniUser>).data
        assertEquals(email, loggedInUser.email)
    }
}
