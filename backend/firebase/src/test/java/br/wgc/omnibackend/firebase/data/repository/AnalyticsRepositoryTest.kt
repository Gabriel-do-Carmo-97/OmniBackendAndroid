package br.wgc.omnibackend.firebase.data.repository

import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AnalyticsRepositoryTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private lateinit var repository: AnalyticsRepositoryImpl

    @Before
    fun setUp() {
        repository = AnalyticsRepositoryImpl(firebaseAnalytics)
    }

    @Test
    fun `setUserProperty delegates to FirebaseAnalytics`() {
        repository.setUserProperty("theme", "dark")
        verify(exactly = 1) { firebaseAnalytics.setUserProperty("theme", "dark") }
    }

    @Test
    fun `setUserId delegates to FirebaseAnalytics`() {
        repository.setUserId("user_123")
        verify(exactly = 1) { firebaseAnalytics.setUserId("user_123") }
    }

    @Test
    fun `logEvent delegates to FirebaseAnalytics with bundle`() {
        repository.logEvent("purchase", mapOf("item_id" to "123", "price" to 49.99))
        verify(exactly = 1) { firebaseAnalytics.logEvent("purchase", any()) }
    }
}
