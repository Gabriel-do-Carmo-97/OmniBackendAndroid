package br.wgc.omnibackend.core.network

/**
 * Contrato corporativo para fornecimento, cache e renovação atômica de tokens de autenticação (JWT).
 *
 * Utilizado pelos interceptors HTTP do OmniBackend para injetar credenciais e tratar expirações 401.
 */
interface TokenProvider {
    /** Retorna o token de acesso atualmente em cache ou memória, de forma não-bloqueante. */
    fun getCachedAccessToken(): String?

    /** Obtém o token de acesso atualizado de forma assíncrona/suspensa. */
    suspend fun getAccessToken(): String?

    /**
     * Executa a renovação síncrona/atômica do token junto ao provedor de identidade.
     * Retorna o novo token se renovado com sucesso, ou null se a sessão for irrecuperável.
     */
    suspend fun refreshToken(): String?

    /** Notifica que a sessão do usuário expirou definitivamente e requer re-autenticação. */
    fun onSessionExpired()
}
