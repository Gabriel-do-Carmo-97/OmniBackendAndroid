package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.firebase.domain.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsRepository {

    override fun trackAppOpenEvent(
        screenName: String,
        screenClass: String
    ) = analytics.logEvent(
        name = FirebaseAnalytics.Event.APP_OPEN,
    ) {
        param(FirebaseAnalytics.Param.CONTENT_TYPE, "event")
        param(FirebaseAnalytics.Param.METHOD, "trackAppOpenEvent")
        param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
    }

    override fun trackScreenViewEvent(
        screenName: String,
        screenClass: String
    ) = analytics.logEvent(
        name = FirebaseAnalytics.Event.SCREEN_VIEW,
    ) {
        param(FirebaseAnalytics.Param.CONTENT_TYPE, "event")
        param(FirebaseAnalytics.Param.METHOD, "trackScreenViewEvent")
        param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = android.os.Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        analytics.logEvent(name, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }
}
