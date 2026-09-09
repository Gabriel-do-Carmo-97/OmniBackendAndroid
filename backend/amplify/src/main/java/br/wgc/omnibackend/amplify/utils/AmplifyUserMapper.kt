package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.model.OmniUser

/**
 * Mapeia respostas do AWS Cognito para [OmniUser].
 */
internal object AmplifyUserMapper {

    /**
     * Mapeia um mapa de atributos de usuário do AWS Cognito/Amplify para [OmniUser].
     *
     * @param attributes Atributos do usuário Cognito.
     * @return [OmniUser] correspondente.
     */
    fun toOmniUser(attributes: Map<String, Any?>): OmniUser {
        val uid = attributes["sub"]?.toString() ?: attributes["id"]?.toString().orEmpty()
        val email = attributes["email"]?.toString().orEmpty()
        val name = attributes["name"]?.toString() ?: attributes["preferred_username"]?.toString()
        val picture = attributes["picture"]?.toString()
        val verified = attributes["email_verified"] as? Boolean ?: false

        return OmniUser(
            uid = uid,
            email = email,
            displayName = name,
            photoUrl = picture,
            isEmailVerified = verified,
            isAnonymous = email.isBlank()
        )
    }
}
