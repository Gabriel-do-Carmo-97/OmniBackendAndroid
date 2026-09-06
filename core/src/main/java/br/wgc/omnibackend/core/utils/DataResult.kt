package br.wgc.omnibackend.core.utils

/**
 * Encapsula o resultado de uma operação executada na camada de repositório ou infraestrutura.
 *
 * Adota a semântica funcional de mônada Result, segregando explicitamente sucessos com valor ([Success])
 * de falhas tipadas na hierarquia unificada de erros do ecossistema ([Failure]).
 *
 * Exemplo de uso:
 * ```kotlin
 * val result: DataResult<OmniUser> = authRepository.login("user@test.com", "pass")
 * when (result) {
 *     is DataResult.Success -> println("Logado com sucesso: ${result.data.displayName}")
 *     is DataResult.Failure -> println("Falha na autenticação: ${result.error}")
 * }
 * ```
 *
 * @param T O tipo do dado retornado em caso de sucesso.
 */
sealed interface DataResult<out T> {

    /**
     * Representa uma operação concluída com êxito.
     *
     * @property data O payload de resposta gerado pela operação.
     */
    data class Success<out T>(val data: T) : DataResult<T>

    /**
     * Representa uma operação com falha previsível ou erro de infraestrutura.
     *
     * @property error A falha estruturada na hierarquia [AppError].
     */
    data class Failure(val error: AppError) : DataResult<Nothing>

    /** Retorna `true` se o resultado for [Success]. */
    val isSuccess: Boolean get() = this is Success

    /** Retorna `true` se o resultado for [Failure]. */
    val isFailure: Boolean get() = this is Failure

    /**
     * Retorna o dado contido em caso de sucesso ou `null` caso tenha ocorrido falha.
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Retorna o erro contido em caso de falha ou `null` caso tenha ocorrido sucesso.
     */
    fun errorOrNull(): AppError? = (this as? Failure)?.error
}

/**
 * Executa a ação [action] se este resultado for [DataResult.Success].
 *
 * @param action Lambda recebendo o valor [T].
 * @return O próprio [DataResult] para encadeamento fluente.
 */
inline fun <T> DataResult<T>.onSuccess(action: (value: T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

/**
 * Executa a ação [action] se este resultado for [DataResult.Failure].
 *
 * @param action Lambda recebendo o erro [AppError].
 * @return O próprio [DataResult] para encadeamento fluente.
 */
inline fun <T> DataResult<T>.onFailure(action: (error: AppError) -> Unit): DataResult<T> {
    if (this is DataResult.Failure) action(error)
    return this
}

/**
 * Transforma o valor [T] contido em caso de sucesso usando a função [transform].
 *
 * @param transform Função de transformação do dado.
 * @return Novo [DataResult] com o dado transformado ou a falha original.
 */
inline fun <T, R> DataResult<T>.map(transform: (value: T) -> R): DataResult<R> {
    return when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Failure -> DataResult.Failure(error)
    }
}

/**
 * Reduz o resultado aplicando [onSuccess] se for [DataResult.Success], ou [onFailure] se for [DataResult.Failure].
 *
 * @param onSuccess Função executada com o dado [T] de sucesso.
 * @param onFailure Função executada com o erro [AppError] em caso de falha.
 * @return O valor resultante [R].
 */
inline fun <T, R> DataResult<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (error: AppError) -> R
): R {
    return when (this) {
        is DataResult.Success -> onSuccess(data)
        is DataResult.Failure -> onFailure(error)
    }
}

