package br.wgc.omnibackend.core.offline

import br.wgc.omnibackend.core.network.NetworkMonitor
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockRemoteRepo: FirestoreRepository = mockk(relaxed = true)
    private val mockQueue: MutationQueueRepository = mockk(relaxed = true)
    private val isOnlineFlow = MutableStateFlow(false)
    private val mockNetworkMonitor: NetworkMonitor = mockk {
        coEvery { isOnline } returns isOnlineFlow
    }

    private lateinit var offlineRepo: OfflineFirstRepository

    @Before
    fun setUp() {
        offlineRepo = OfflineFirstRepository(
            remoteRepository = mockRemoteRepo,
            mutationQueue = mockQueue,
            networkMonitor = mockNetworkMonitor,
            scope = testScope,
        )
    }

    @Test
    fun `addDocument delegates to remote when online`() = runTest {
        coEvery { mockRemoteRepo.addDocument("users", "data", "doc_1") } returns DataResult.Success("doc_1")

        val result = offlineRepo.addDocument("users", "data", "doc_1")

        assertTrue(result is DataResult.Success)
        assertEquals("doc_1", (result as DataResult.Success).data)
    }

    @Test
    fun `addDocument enqueues mutation when remote fails`() = runTest {
        coEvery { mockRemoteRepo.addDocument("users", "data", "doc_1") } returns DataResult.Failure(AppError.Generic.Network)

        val result = offlineRepo.addDocument("users", "data", "doc_1")

        assertTrue(result is DataResult.Success)
        coVerify { mockQueue.enqueue(any()) }
    }
}
