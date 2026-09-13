package br.wgc.omnibackend.core.model.database.geo

import androidx.annotation.Keep

/**
 * Representa as coordenadas geográficas de uma entidade em um determinado instante de tempo.
 *
 * @property latitude Coordenada de latitude em graus decimais (intervalo [-90.0, 90.0]).
 * @property longitude Coordenada de longitude em graus decimais (intervalo [-180.0, 180.0]).
 * @property timestamp Carimbo de data/hora em milissegundos UTC no momento da captura da posição.
 */
@Keep
data class LocationRequest(val latitude: Double = 0.0, val longitude: Double = 0.0, val timestamp: Long = System.currentTimeMillis())
