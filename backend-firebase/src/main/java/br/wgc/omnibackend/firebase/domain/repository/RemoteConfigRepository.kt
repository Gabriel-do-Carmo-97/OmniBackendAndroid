package br.wgc.omnibackend.firebase.domain.repository

import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult

/**
 * Interface for the repository that handles Firebase Remote Config.
 */
interface RemoteConfigRepository {

    /**
     * Fetches the latest remote config values from the server and activates them.
     * This makes the new values available for your app.
     * @return true if the fetched configs were successfully activated, false otherwise.
     */
    suspend fun fetchAndActivate(): DataResult<Boolean>


    /**
     * Gets a string value for a given key from the activated config.
     * @param key The key for the config value.
     * @return The String value associated with the key.
     */
    fun getString(key: String): DataResult<String>


    /**
     * Gets a boolean value for a given key from the activated config.
     * @param key The key for the config value.
     * @return The Boolean value associated with the key.
     */
    fun getBoolean(key: String): DataResult<Boolean>

    /**
     * Gets a long value for a given key from the activated config.
     * @param key The key for the config value.
     * @return The Long value associated with the key.
     */
    fun getLong(key: String): DataResult<Long>

    /**
     * Gets a double value for a given key from the activated config.
     * @param key The key for the config value.
     * @return The Double value associated with the key.
     */
    fun getDouble(key: String): DataResult<Double>

    /**
     * Observa atualizações de uma chave do Remote Config em tempo real como um [Flow].
     */
    fun observeKeyUpdates(key: String): kotlinx.coroutines.flow.Flow<DataResult<Unit>>

    fun detectedUpdateFlagInLive(
        key: String,
        updateFlagResult: suspend () -> Unit,
        errorResult: suspend (AppError) -> Unit
    )
}

