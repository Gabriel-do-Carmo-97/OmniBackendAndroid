package br.wgc.omnibackend.firebase.utils

import br.wgc.omnibackend.core.utils.AppError

/**
 * Uma classe selada para representar os diferentes estados de uma operação de caso de uso,
 * especialmente útil para a camada de ViewModel atualizar a UI.
 *
 * @param T O tipo de dados que será retornado em caso de sucesso.
 */
sealed class UseCaseResult<out T> {
    /**
     * Indica que a operação está em andamento.
     */
    object Loading : UseCaseResult<Nothing>()

    /**
     * Indica que a operação foi concluída com sucesso.
     * @param data Os dados resultantes da operação.
     */
    data class Success<out T>(val data: T) : UseCaseResult<T>()

    /**
     * Indica que a operação falhou.
     * @param error O erro que ocorreu durante a operação.
     */
    data class Failure(val error: AppError) : UseCaseResult<Nothing>()
}

