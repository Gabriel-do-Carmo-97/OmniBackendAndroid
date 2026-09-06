package br.wgc.omnibackend.core.repository.realtime

import br.wgc.omnibackend.core.model.database.presence.PresenceStateRequest
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para monitoramento e gestão do estado de presença (online/offline) de entidades.
 */
interface PresenceRepository {

    /**
     * Marca a entidade como online e registra hooks de desconexão automática no servidor.
     *
     * @param entityType Categoria da entidade (ex: "users", "couriers").
     * @param entityId Identificador da entidade.
     * @return [DataResult.Success] com [Unit] após a confirmação no servidor.
     */
    suspend fun goOnline(entityType: String, entityId: String): DataResult<Unit>

    /**
     * Marca a entidade explicitamente como offline (ex: no logout intencional).
     *
     * @param entityType Categoria da entidade.
     * @param entityId Identificador da entidade.
     * @return [DataResult.Success] com [Unit] após a confirmação no servidor.
     */
    suspend fun goOffline(entityType: String, entityId: String): DataResult<Unit>

    /**
     * Observa o status de presença de uma entidade em tempo real.
     *
     * @param entityType Categoria da entidade.
     * @param entityId Identificador da entidade monitorada.
     * @return [Flow] emitindo o estado de presença ([PresenceStateRequest]) à medida que muda.
     */
    fun trackPresence(entityType: String, entityId: String): Flow<DataResult<PresenceStateRequest>>
}
