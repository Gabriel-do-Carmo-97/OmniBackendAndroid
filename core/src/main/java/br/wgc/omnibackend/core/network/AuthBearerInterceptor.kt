package br.wgc.omnibackend.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor do OkHttp que injeta de forma transparente o cabeçalho HTTP `Authorization: Bearer <token>`
 * obtido a partir de [TokenProvider] caso a requisição não possua autenticação explícita já definida.
 *
 * @property tokenProvider Provedor responsável por fornecer o token de autenticação atual.
 */
class AuthBearerInterceptor(private val tokenProvider: TokenProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runCatching { tokenProvider.getCachedAccessToken() }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: runCatching {
                runBlocking { tokenProvider.getAccessToken() }
            }.getOrNull()

        val request = if (!token.isNullOrBlank() && originalRequest.header(AUTHORIZATION_HEADER) == null) {
            originalRequest.newBuilder()
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
