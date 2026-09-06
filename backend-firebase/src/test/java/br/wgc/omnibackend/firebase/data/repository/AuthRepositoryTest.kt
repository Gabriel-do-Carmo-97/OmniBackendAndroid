package br.wgc.omnibackend.firebase.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val mockUser: FirebaseUser = mockk(relaxed = true)
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(firebaseAuth)
    }

    @Test
    fun `authState emits current Firebase user from listener`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
            every { firebaseAuth.currentUser } returns mockUser
            listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        }
        every { firebaseAuth.removeAuthStateListener(any()) } returns Unit

        val user = repository.authState.first()

        assertEquals(mockUser, user)
        verify { firebaseAuth.addAuthStateListener(any()) }
    }
}

