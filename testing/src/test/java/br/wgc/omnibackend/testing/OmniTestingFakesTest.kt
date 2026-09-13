package br.wgc.omnibackend.testing

import android.net.Uri
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class TestPayload(val name: String, val age: Int)

class OmniTestingFakesTest {

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers { mockk(relaxed = true) }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `fake auth registers and logs in user successfully`() = runTest {
        val auth = FakeAuthRepository()

        val regResult = auth.createUser("dev@company.com", "secret123")
        assertTrue(regResult is DataResult.Success)

        val loginResult = auth.login("dev@company.com", "secret123")
        assertTrue(loginResult is DataResult.Success)
        assertEquals("dev@company.com", (loginResult as DataResult.Success).data.email)
        assertEquals("dev@company.com", auth.currentUser?.email)

        val wrongPass = auth.login("dev@company.com", "wrong")
        assertTrue(wrongPass is DataResult.Failure)
    }

    @Test
    fun `fake auth responds to simulated errors`() = runTest {
        val auth = FakeAuthRepository()
        auth.simulatedError = AppError.Generic.Network

        val result = auth.login("any@mail.com", "pass")
        assertTrue(result is DataResult.Failure)
        assertEquals(AppError.Generic.Network, (result as DataResult.Failure).error)
    }

    @Test
    fun `fake firestore performs CRUD operations in memory`() = runTest {
        val db = FakeFirestoreRepository()

        val addResult = db.addDocument("users", TestPayload("Gabriel", 29), "user_1")
        assertTrue(addResult is DataResult.Success)
        assertEquals("user_1", (addResult as DataResult.Success).data)

        val getResult = db.getDocument("users", "user_1", TestPayload::class.java)
        assertTrue(getResult is DataResult.Success)
        val payload = (getResult as DataResult.Success).data
        assertNotNull(payload)
        assertEquals("Gabriel", payload?.name)
        assertEquals(29, payload?.age)

        val delResult = db.deleteDocument("users", "user_1")
        assertTrue(delResult is DataResult.Success)

        val getAfterDelete = db.getDocument("users", "user_1", TestPayload::class.java)
        assertTrue(getAfterDelete is DataResult.Success)
        assertNull((getAfterDelete as DataResult.Success).data)
    }

    @Test
    fun `fake storage stores bytes and generates uri`() = runTest {
        val storage = FakeStorageRepository()
        val data = "Hello OmniBackend".toByteArray()

        val uploadResult = storage.uploadFileDirect("docs/test.txt", data)
        assertTrue(uploadResult is DataResult.Success)

        val downloadResult = storage.getDownloadUrl("docs/test.txt")
        assertTrue(downloadResult is DataResult.Success)
        assertNotNull((downloadResult as DataResult.Success).data)

        val delResult = storage.delete("docs/test.txt")
        assertTrue(delResult is DataResult.Success)

        val downloadAfterDel = storage.getDownloadUrl("docs/test.txt")
        assertTrue(downloadAfterDel is DataResult.Failure)
    }
}
