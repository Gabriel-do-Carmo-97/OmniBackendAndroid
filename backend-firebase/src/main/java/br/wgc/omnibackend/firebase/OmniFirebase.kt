package br.wgc.omnibackend.firebase

import android.content.Context
import br.wgc.omnibackend.firebase.data.repository.AnalyticsRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.AuthRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.FirestoreRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.RealtimeDatabaseRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.RemoteConfigRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.StorageRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.VertexAIRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.realtime.GeolocationRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.realtime.MessageRepositoryImpl
import br.wgc.omnibackend.firebase.data.repository.realtime.PresenceRepositoryImpl
import br.wgc.omnibackend.firebase.domain.repository.AnalyticsRepository
import br.wgc.omnibackend.firebase.domain.repository.AuthRepository
import br.wgc.omnibackend.firebase.domain.repository.FirestoreRepository
import br.wgc.omnibackend.firebase.domain.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.firebase.domain.repository.RemoteConfigRepository
import br.wgc.omnibackend.firebase.domain.repository.StorageRepository
import br.wgc.omnibackend.firebase.domain.repository.VertexAIRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.GeolocationRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.MessageRepository
import br.wgc.omnibackend.firebase.domain.repository.realtime.PresenceRepository
import br.wgc.omnibackend.firebase.security.AppCheckManager
import br.wgc.omnibackend.firebase.telemetry.FirebaseTelemetry
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.storage.storage
import com.google.firebase.vertexai.vertexAI

/**
 * Enterprise Facade entry-point for the WGC Firebase SDK.
 *
 * Provides thread-safe, lazy-initialized access to all Firebase repositories and tools.
 * Can be used directly without dependency injection frameworks or seamlessly alongside Hilt/Koin.
 *
 * Example usage:
 * ```kotlin
 * // In Application.onCreate():
 * OmniFirebase.initialize(this, enableAppCheck = true, isDebug = BuildConfig.DEBUG)
 *
 * // In features/presenters/viewmodels:
 * val userFlow = OmniFirebase.auth.authState
 * val firestore = OmniFirebase.firestore
 * OmniFirebase.telemetry.recordError(error)
 * ```
 */
object OmniFirebase {

    @Volatile
    private var isInitialized = false

    /**
     * Initializes Firebase and SDK security features.
     *
     * @param context Application context.
     * @param enableAppCheck Whether to enable Firebase App Check (default false).
     * @param isDebug Whether running in debug mode (uses Debug App Check Provider).
     */
    fun initialize(
        context: Context,
        enableAppCheck: Boolean = false,
        isDebug: Boolean = false
    ) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    if (FirebaseApp.getApps(context).isEmpty()) {
                        FirebaseApp.initializeApp(context)
                    }
                    if (enableAppCheck) {
                        AppCheckManager.initialize(isDebug = isDebug)
                    }
                    isInitialized = true
                }
            }
        }
    }

    /** Authentication operations and reactive user session flow. */
    val auth: AuthRepository by lazy { AuthRepositoryImpl(Firebase.auth) }

    /** Cloud Firestore document and collection operations. */
    val firestore: FirestoreRepository by lazy { FirestoreRepositoryImpl(Firebase.firestore) }

    /** Realtime Geolocation tracking and spatial queries. */
    val geolocation: GeolocationRepository by lazy { GeolocationRepositoryImpl(Firebase.database) }

    /** Realtime Messaging and chat operations. */
    val message: MessageRepository by lazy { MessageRepositoryImpl(Firebase.database) }

    /** Realtime Presence and connection status tracking. */
    val presence: PresenceRepository by lazy { PresenceRepositoryImpl(Firebase.database) }

    /** Realtime Database core operations and sub-repositories. */
    val realtimeDatabase: RealtimeDatabaseRepository by lazy {
        RealtimeDatabaseRepositoryImpl(
            messageRepository = message,
            geolocationRepository = geolocation,
            presenceRepository = presence
        )
    }

    /** Cloud Storage file upload, download, and delete operations. */
    val storage: StorageRepository by lazy { StorageRepositoryImpl(Firebase.storage) }

    /** Firebase Remote Config fetching and parameter retrieval. */
    val remoteConfig: RemoteConfigRepository by lazy { RemoteConfigRepositoryImpl(Firebase.remoteConfig) }

    /** Firebase Analytics event logging and user properties. */
    val analytics: AnalyticsRepository by lazy { AnalyticsRepositoryImpl(Firebase.analytics) }

    /** Vertex AI generative AI operations (Gemini). */
    val vertexAI: VertexAIRepository by lazy { VertexAIRepositoryImpl(Firebase.vertexAI()) }

    /** Observability and crash reporting helper. */
    val telemetry: FirebaseTelemetry by lazy { FirebaseTelemetry.get() }

    /** App Check configuration helper. */
    val appCheck: AppCheckManager get() = AppCheckManager
}

/**
 * Backward compatibility alias for WgcFirebase.
 */
typealias WgcFirebase = OmniFirebase

