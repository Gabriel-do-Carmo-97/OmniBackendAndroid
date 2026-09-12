package br.wgc.omnibackend.bundle.supabasecloudflare

import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniSupabaseCloudflareTest {

    private val mockPrimaryAuth = mockk<AuthRepository>()
    private val mockSecondaryAuth = mockk<AuthRepository>()
    private val mockPrimaryDb = mockk<FirestoreRepository>()
    private val mockSecondaryDb = mockk<FirestoreRepository>()
    private val mockPrimaryStorage = mockk<StorageRepository>()
    private val mockSecondaryStorage = mockk<StorageRepository>()

    @Test
    fun `facade exposes drivers and hybrid engine correctly`() {
        assertNotNull(OmniSupabaseCloudflare.supabase)
        assertNotNull(OmniSupabaseCloudflare.cloudflare)
        assertNotNull(OmniSupabaseCloudflare.hybrid)
    }

    @Test
    fun `facade creates hybrid repositories successfully`() {
        val auth = OmniSupabaseCloudflare.createAuth(mockPrimaryAuth, mockSecondaryAuth)
        val db = OmniSupabaseCloudflare.createDatabase(mockPrimaryDb, mockSecondaryDb)
        val storage = OmniSupabaseCloudflare.createStorage(mockPrimaryStorage, mockSecondaryStorage)

        assertNotNull(auth)
        assertNotNull(db)
        assertNotNull(storage)
    }
}
