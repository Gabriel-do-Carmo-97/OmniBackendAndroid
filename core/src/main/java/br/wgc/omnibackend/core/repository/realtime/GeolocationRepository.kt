package br.wgc.omnibackend.core.repository.realtime

import br.wgc.omnibackend.core.model.database.geo.LocationRequest
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para rastreamento e atualização de coordenadas geográficas em tempo real.
 */
interface GeolocationRepository {

    /**
     * Atualiza as coordenadas geográficas de uma entidade (usuário, motorista, entregador).
     *
     * @param entityType Categoria ou nó pai da entidade (ex: "users", "drivers").
     * @param entityId Identificador exclusivo da entidade.
     * @param location Coordenadas geográficas e timestamp da posição ([LocationRequest]).
     * @return [DataResult.Success] com o identificador da entidade atualizada.
     */
    suspend fun updateLocation(entityType: String, entityId: String, location: LocationRequest): DataResult<String>

    /**
     * Observa o fluxo contínuo de coordenadas de uma entidade em tempo real.
     *
     * @param entityType Categoria da entidade.
     * @param entityId Identificador da entidade.
     * @return [Flow] emitindo as coordenadas à medida que se movem no mapa.
     */
    fun trackLocation(entityType: String, entityId: String): Flow<DataResult<LocationRequest>>
}
