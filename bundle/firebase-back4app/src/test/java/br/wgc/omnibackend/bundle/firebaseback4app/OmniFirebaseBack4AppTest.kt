package br.wgc.omnibackend.bundle.firebaseback4app

import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniFirebaseBack4AppTest {

    private val mockPrimaryAuth = mockk<AuthRepository>()
    private val mockSecondaryAuth = mockk<AuthRepository>()
    private val mockPrimaryDb = mockk<FirestoreRepository>()
    private val mockSecondaryDb = mockk<FirestoreRepository>()
    private val mockPrimaryStorage = mockk<StorageRepository>()
    private val mockSecondaryStorage = mockk<StorageRepository>()

    @Test
    fun `facade exposes drivers and hybrid engine correctly`() {
        assertNotNull(OmniFirebaseBack4App.firebase)
        assertNotNull(OmniFirebaseBack4App.back4app)
        assertNotNull(OmniFirebaseBack4App.hybrid)
    }

    @Test
    fun `facade creates hybrid repositories successfully`() {
        val auth = OmniFirebaseBack4App.createAuth(mockPrimaryAuth, mockSecondaryAuth)
        val db = OmniFirebaseBack4App.createDatabase(mockPrimaryDb, mockSecondaryDb)
        val storage = OmniFirebaseBack4App.createStorage(mockPrimaryStorage, mockSecondaryStorage)

        assertNotNull(auth)
        assertNotNull(db)
        assertNotNull(storage)
    }
}
