package br.wgc.omnibackend.cloudflare.utils

import br.wgc.omnibackend.core.model.OmniUser

/**
 * Mapeia respostas de usuário de Cloudflare Workers / Turnstile / Zero Trust para [OmniUser].
 */
internal object CloudflareUserMapper {

    /**
     * Converte um mapa de propriedades de usuário retornado por um Cloudflare Worker para [OmniUser].
     *
     * @param data Mapa de dados da resposta.
     * @return [OmniUser] correspondente.
     */
    fun toOmniUser(data: Map<String, Any?>): OmniUser {
        val uid = data["uid"]?.toString() ?: data["id"]?.toString().orEmpty()
        val email = data["email"]?.toString().orEmpty()
        val displayName = data["displayName"]?.toString() ?: data["name"]?.toString()
        val photoUrl = data["photoUrl"]?.toString() ?: data["avatar"]?.toString()
        val isVerified = data["isEmailVerified"] as? Boolean ?: false
        val isAnonymous = data["isAnonymous"] as? Boolean ?: email.isBlank()

        return OmniUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isEmailVerified = isVerified,
            isAnonymous = isAnonymous,
        )
    }
}
