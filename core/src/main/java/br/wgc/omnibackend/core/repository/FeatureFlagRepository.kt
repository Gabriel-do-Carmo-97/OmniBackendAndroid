package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.utils.DataResult

/**
 * Contrato agnóstico para gerenciamento de Feature Flags e A/B Testing (Firebase Remote Config, LaunchDarkly, DB).
 */
interface FeatureFlagRepository {

    /**
     * Busca e ativa as flags mais recentes do servidor remoto.
     */
    suspend fun fetchAndActivate(): DataResult<Boolean>

    /**
     * Retorna o valor booleano da flag informada, ou [defaultValue] se inexistente.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * Retorna o valor de texto da flag informada, ou [defaultValue] se inexistente.
     */
    fun getString(key: String, defaultValue: String = ""): String

    /**
     * Retorna o valor numérico (Long) da flag informada, ou [defaultValue] se inexistente.
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long
}
