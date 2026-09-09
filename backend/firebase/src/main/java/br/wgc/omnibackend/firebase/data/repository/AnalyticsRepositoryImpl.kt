package br.wgc.omnibackend.firebase.data.repository

import android.os.Bundle
import br.wgc.omnibackend.core.repository.AnalyticsRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject

/**
 * Implementação do contrato [AnalyticsRepository] utilizando o Google Analytics for Firebase.
 *
 * @property analytics Instância do [FirebaseAnalytics] injetada.
 */
class AnalyticsRepositoryImpl @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsRepository {

    /**
     * Rastreia o evento de abertura do aplicativo.
     */
    override fun trackAppOpen() {
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "event")
        }
    }

    /**
     * Rastreia a visualização de uma tela pelo usuário.
     *
     * @param screenName Nome da tela.
     * @param screenClass Nome da classe ou componente de tela (opcional).
     */
    override fun trackScreenView(screenName: String, screenClass: String?) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
    }

    /**
     * Registra um evento de telemetria customizado com mapa de propriedades.
     *
     * @param eventName Nome do evento.
     * @param params Mapa chave-valor de propriedades contextuais.
     */
    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        val bundle = Bundle().apply {
            params?.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    /**
     * Define uma propriedade de usuário duradoura no Firebase Analytics.
     *
     * @param name Nome da propriedade.
     * @param value Valor associado.
     */
    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    /**
     * Define o identificador único de usuário no Firebase Analytics.
     *
     * @param userId Identificador exclusivo.
     */
    override fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }
}
