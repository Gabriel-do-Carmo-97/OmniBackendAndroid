package br.wgc.omnibackend.firebase.security

import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Manages Firebase App Check configuration and initialization.
 * Protects backend services (Firestore, Realtime DB, Cloud Storage) from unauthorized clients.
 */
object AppCheckManager {

    /**
     * Installs the appropriate App Check provider factory.
     *
     * @param isDebug If true, installs [DebugAppCheckProviderFactory] which prints a debug token to Logcat.
     *                If false, installs [PlayIntegrityAppCheckProviderFactory] for production Play Store verification.
     */
    fun initialize(isDebug: Boolean = false) {
        val appCheck = FirebaseAppCheck.getInstance()
        val factory = if (isDebug) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        appCheck.installAppCheckProviderFactory(factory)
    }

    /**
     * Enables or disables automatic token refreshing for App Check.
     */
    fun setTokenAutoRefreshEnabled(enabled: Boolean) {
        FirebaseAppCheck.getInstance().setTokenAutoRefreshEnabled(enabled)
    }
}

