package br.wgc.omnibackend.firebase.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirestoreRepositoryTest {

    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val collectionRef: CollectionReference = mockk(relaxed = true)
    private val documentRef: DocumentReference = mockk(relaxed = true)

    private lateinit var repository: FirestoreRepositoryImpl

    @Before
    fun setUp() {
        every { firestore.collection(any()) } returns collectionRef
        every { collectionRef.document(any()) } returns documentRef
        every { documentRef.set(any()) } returns Tasks.forResult(null)
        every { documentRef.id } returns "user_123"
        repository = FirestoreRepositoryImpl(firestore)
    }

    @Test
    fun `addDocument delegates to firestore collection and document`() = runTest {
        val data = mapOf("name" to "Test")
        repository.addDocument("users", data, "user_123")
        verify(exactly = 1) { firestore.collection("users") }
        verify(exactly = 1) { collectionRef.document("user_123") }
    }
}

