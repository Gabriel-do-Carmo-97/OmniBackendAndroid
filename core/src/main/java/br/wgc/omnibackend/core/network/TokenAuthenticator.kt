package br.wgc.omnibackend.core.network

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * [Authenticator] do OkHttp responsável por tratar respostas `401 Unauthorized`.
 *
 * Utiliza [Mutex] para serializar requisições concorrentes que falham simultaneamente,
 * disparando a renovação através de [TokenProvider.refreshToken] exatamente uma única vez
 * e reenviando todas as requisições com o novo token Bearer de forma transparente.
 *
 * @property tokenProvider Provedor de renovação e persistência de credenciais.
 * @property refreshMutex Mutex de sincronização para coordenar múltiplas chamadas simultâneas.
 */
class TokenAuthenticator(private val tokenProvider: TokenProvider, private val refreshMutex: Mutex = Mutex()) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRY_ATTEMPTS) {
            return null
        }

        val currentHeader = response.request.header(AUTHORIZATION_HEADER)

        val newAccessToken: String? = runBlocking {
            refreshMutex.withLock {
                val latestToken = tokenProvider.getAccessToken()
                if (!latestToken.isNullOrBlank() && "$BEARER_PREFIX$latestToken" != currentHeader) {
                    latestToken
                } else {
                    val refreshed = tokenProvider.refreshToken()
                    if (refreshed == null) {
                        tokenProvider.onSessionExpired()
                    }
                    refreshed
                }
            }
        }

        return if (!newAccessToken.isNullOrBlank()) {
            response.request.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$newAccessToken")
                .build()
        } else {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}
