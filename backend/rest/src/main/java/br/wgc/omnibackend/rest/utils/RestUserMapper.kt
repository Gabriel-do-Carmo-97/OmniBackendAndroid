package br.wgc.omnibackend.rest.utils

import br.wgc.omnibackend.core.model.OmniUser

/**
 * Mapeia respostas JSON de usuário de APIs REST corporativas para [OmniUser].
 */
internal object RestUserMapper {

    /**
     * Mapeia um mapa de dados da API REST para [OmniUser].
     *
     * @param data Payload JSON convertido em Map.
     * @return [OmniUser] resultante.
     */
    fun toOmniUser(data: Map<String, Any?>): OmniUser {
        val uid = data["id"]?.toString() ?: data["uid"]?.toString().orEmpty()
        val email = data["email"]?.toString().orEmpty()
        val name = data["name"]?.toString() ?: data["displayName"]?.toString()
        val photoUrl = data["photoUrl"]?.toString() ?: data["avatar"]?.toString()
        val verified = data["isEmailVerified"] as? Boolean ?: false
        val anonymous = data["isAnonymous"] as? Boolean ?: email.isBlank()

        return OmniUser(
            uid = uid,
            email = email,
            displayName = name,
            photoUrl = photoUrl,
            isEmailVerified = verified,
            isAnonymous = anonymous
        )
    }
}
