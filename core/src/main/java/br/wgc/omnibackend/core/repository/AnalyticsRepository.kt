package br.wgc.omnibackend.core.repository

/**
 * Contrato agnóstico para eventos de telemetria e analytics.
 */
interface AnalyticsRepository {
    fun logEvent(eventName: String, params: Map<String, Any>?)
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String)
    fun trackScreenView(screenName: String, screenClass: String?)
    fun trackAppOpen()
}
