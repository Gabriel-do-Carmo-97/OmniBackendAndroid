package br.wgc.omnibackend.bundle.hybrid

import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniHybridTest {

    private val primaryAuth = mockk<AuthRepository>()
    private val secondaryAuth = mockk<AuthRepository>()

    private val primaryDb = mockk<FirestoreRepository>()
    private val secondaryDb = mockk<FirestoreRepository>()

    private val primaryStorage = mockk<StorageRepository>()
    private val secondaryStorage = mockk<StorageRepository>()

    @Test
    fun `hybrid auth succeeds with primary when available`() = runTest {
        coEvery { primaryAuth.login("test@mail.com", "pass") } returns DataResult.Success(mockk())

        val hybridAuth = OmniHybrid.createAuth(primaryAuth, secondaryAuth)
        val result = hybridAuth.login("test@mail.com", "pass")

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `hybrid auth falls back to secondary when primary fails`() = runTest {
        coEvery { primaryAuth.login("test@mail.com", "pass") } returns DataResult.Failure(AppError.Generic.Network)
        coEvery { secondaryAuth.login("test@mail.com", "pass") } returns DataResult.Success(mockk())

        val hybridAuth = OmniHybrid.createAuth(primaryAuth, secondaryAuth)
        val result = hybridAuth.login("test@mail.com", "pass")

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `hybrid database falls back to secondary when primary fails`() = runTest {
        coEvery { primaryDb.getDocument("users", "123", String::class.java) } returns DataResult.Failure(AppError.Generic.Network)
        coEvery { secondaryDb.getDocument("users", "123", String::class.java) } returns DataResult.Success("fallback_data")

        val hybridDb = OmniHybrid.createDatabase(primaryDb, secondaryDb)
        val result = hybridDb.getDocument("users", "123", String::class.java)

        assertTrue(result is DataResult.Success)
        assertEquals("fallback_data", (result as DataResult.Success).data)
    }

    @Test
    fun `hybrid storage falls back to secondary when primary fails`() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val uri = mockk<android.net.Uri>()
        coEvery { primaryStorage.uploadFileDirect("path", bytes) } returns DataResult.Failure(AppError.Generic.Network)
        coEvery { secondaryStorage.uploadFileDirect("path", bytes) } returns DataResult.Success(uri)

        val hybridStorage = OmniHybrid.createStorage(primaryStorage, secondaryStorage)
        val result = hybridStorage.uploadFileDirect("path", bytes)

        assertTrue(result is DataResult.Success)
        assertEquals(uri, (result as DataResult.Success).data)
    }
}
