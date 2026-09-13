package br.wgc.omnibackend.core.featureflag

import br.wgc.omnibackend.core.repository.FeatureFlagRepository
import br.wgc.omnibackend.core.utils.DataResult
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementação corporativa de [FeatureFlagRepository] com suporte a **cache local, fallback e overrides em runtime**.
 *
 * Permite definir overrides locais (úteis para testes unitários ou QA) que prevalecem sobre os valores remotos do [remoteRepository].
 *
 * @param remoteRepository Provedor remoto de Feature Flags (ex: RemoteConfigRepository).
 */
class HybridFeatureFlagRepository(private val remoteRepository: FeatureFlagRepository? = null) : FeatureFlagRepository {

    private val localOverrides = ConcurrentHashMap<String, Any>()

    override suspend fun fetchAndActivate(): DataResult<Boolean> = remoteRepository?.fetchAndActivate() ?: DataResult.Success(true)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val override = localOverrides[key] as? Boolean
        if (override != null) return override
        return remoteRepository?.getBoolean(key, defaultValue) ?: defaultValue
    }

    override fun getString(key: String, defaultValue: String): String {
        val override = localOverrides[key] as? String
        if (override != null) return override
        return remoteRepository?.getString(key, defaultValue) ?: defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        val override = localOverrides[key] as? Long
        if (override != null) return override
        return remoteRepository?.getLong(key, defaultValue) ?: defaultValue
    }

    /**
     * Define um override local para a flag informada.
     *
     * @param key Chave da flag.
     * @param value Novo valor (Boolean, String ou Long).
     */
    fun setOverride(key: String, value: Any) {
        localOverrides[key] = value
    }

    /**
     * Remove o override local para a chave informada.
     */
    fun clearOverride(key: String) {
        localOverrides.remove(key)
    }

    /**
     * Limpa todos os overrides locais.
     */
    fun clearAllOverrides() {
        localOverrides.clear()
    }
}
