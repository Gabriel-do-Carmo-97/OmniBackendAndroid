package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para obtenção, ativação e escuta de parâmetros dinâmicos de configuração remota.
 */
interface RemoteConfigRepository {

    /**
     * Baixa os valores mais recentes do servidor remoto e os ativa para consumo imediato.
     *
     * @return [DataResult.Success] com `true` se novas configurações foram ativadas com sucesso.
     */
    suspend fun fetchAndActivate(): DataResult<Boolean>

    /**
     * Recupera o valor [String] associado à chave de configuração remota.
     *
     * @param key Identificador da chave no painel de configuração.
     * @return [DataResult.Success] contendo o valor textual.
     */
    fun getString(key: String): DataResult<String>

    /**
     * Recupera o valor [Boolean] associado à chave de configuração remota.
     *
     * @param key Identificador da chave.
     * @return [DataResult.Success] com o valor booleano.
     */
    fun getBoolean(key: String): DataResult<Boolean>

    /**
     * Recupera o valor numérico inteiro [Long] associado à chave de configuração remota.
     *
     * @param key Identificador da chave.
     * @return [DataResult.Success] com o valor [Long].
     */
    fun getLong(key: String): DataResult<Long>

    /**
     * Recupera o valor decimal [Double] associado à chave de configuração remota.
     *
     * @param key Identificador da chave.
     * @return [DataResult.Success] com o valor [Double].
     */
    fun getDouble(key: String): DataResult<Double>

    /**
     * Observa atualizações de uma chave específica em tempo real como um [Flow].
     *
     * @param key Identificador da chave a ser monitorada.
     * @return [Flow] que emite a cada modificação publicada no servidor.
     */
    fun observeKeyUpdates(key: String): Flow<DataResult<Unit>>

    /**
     * Registra listeners de callback para detecção imediata de atualização remota de uma chave.
     *
     * @param key Chave de configuração.
     * @param updateFlagResult Callback executado quando uma atualização for confirmada.
     * @param errorResult Callback executado se houver erro no processamento.
     */
    fun detectedUpdateFlagInLive(key: String, updateFlagResult: suspend () -> Unit, errorResult: suspend (AppError) -> Unit)
}
