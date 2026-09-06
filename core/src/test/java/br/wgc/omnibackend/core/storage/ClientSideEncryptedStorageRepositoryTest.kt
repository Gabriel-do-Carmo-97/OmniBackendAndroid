package br.wgc.omnibackend.core.storage

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.security.KeystoreCryptoManager
import br.wgc.omnibackend.core.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientSideEncryptedStorageRepositoryTest {

    private val mockRemoteStorage: StorageRepository = mockk(relaxed = true)
    private val mockCryptoManager: KeystoreCryptoManager = mockk {
        every { encrypt(any()) } answers { "encrypted:${firstArg<String>()}" }
        every { decrypt(any()) } answers { firstArg<String>().removePrefix("encrypted:") }
    }
    private val mockContext: Context = mockk(relaxed = true)

    private lateinit var encryptedStorageRepo: ClientSideEncryptedStorageRepository

    @Before
    fun setUp() {
        encryptedStorageRepo = ClientSideEncryptedStorageRepository(
            remoteStorage = mockRemoteStorage,
            cryptoManager = mockCryptoManager,
            context = mockContext
        )
    }

    @Test
    fun `uploadFileDirect encrypts bytes before sending to remote`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { mockRemoteStorage.uploadFileDirect("docs/file.txt", any<ByteArray>()) } returns DataResult.Success(mockUri)

        val result = encryptedStorageRepo.uploadFileDirect("docs/file.txt", "my secret data".toByteArray())

        assertTrue(result is DataResult.Success)
        assertEquals(mockUri, (result as DataResult.Success).data)
    }

    @Test
    fun `decryptDownloadedData decrypts payload correctly`() {
        val decrypted = encryptedStorageRepo.decryptDownloadedData("encrypted:my secret data".toByteArray())
        assertEquals("my secret data", decrypted)
    }
}
