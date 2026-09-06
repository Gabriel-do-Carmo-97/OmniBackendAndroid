package br.wgc.omnibackend.firebase.utils

/**
 * Uma sealed class que encapsula o estado de uma operação assíncrona.
 * Permite modelar de forma explícita os estados de Carregando, Sucesso e Falha.
 */
sealed class DataResult<out T> {
    /**
     * Representa o estado de sucesso da operação.
     * @param data Os dados resultantes da operação bem-sucedida.
     */
    data class Success<out T>(val data: T) : DataResult<T>()

    /**
     * Representa o estado de falha da operação.
     * @param error O [AppError] que descreve o que deu errado.
     */
    data class Failure(val error: AppError) : DataResult<Nothing>()
}

