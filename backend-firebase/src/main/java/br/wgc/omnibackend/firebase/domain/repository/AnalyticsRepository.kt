package br.wgc.omnibackend.firebase.domain.repository

interface AnalyticsRepository {
    fun trackAppOpenEvent(screenName: String, screenClass: String)
    fun trackScreenViewEvent(screenName: String, screenClass: String)
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String?)
}
