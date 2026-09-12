package br.wgc.omnibackend.bundle.firebaseamplify

import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniFirebaseAmplifyTest {

    private val mockPrimaryAuth = mockk<AuthRepository>()
    private val mockSecondaryAuth = mockk<AuthRepository>()
    private val mockPrimaryDb = mockk<FirestoreRepository>()
    private val mockSecondaryDb = mockk<FirestoreRepository>()
    private val mockPrimaryStorage = mockk<StorageRepository>()
    private val mockSecondaryStorage = mockk<StorageRepository>()

    @Test
    fun `facade exposes drivers and hybrid engine correctly`() {
        assertNotNull(OmniFirebaseAmplify.firebase)
        assertNotNull(OmniFirebaseAmplify.amplify)
        assertNotNull(OmniFirebaseAmplify.hybrid)
    }

    @Test
    fun `facade creates hybrid repositories successfully`() {
        val auth = OmniFirebaseAmplify.createAuth(mockPrimaryAuth, mockSecondaryAuth)
        val db = OmniFirebaseAmplify.createDatabase(mockPrimaryDb, mockSecondaryDb)
        val storage = OmniFirebaseAmplify.createStorage(mockPrimaryStorage, mockSecondaryStorage)

        assertNotNull(auth)
        assertNotNull(db)
        assertNotNull(storage)
    }
}
