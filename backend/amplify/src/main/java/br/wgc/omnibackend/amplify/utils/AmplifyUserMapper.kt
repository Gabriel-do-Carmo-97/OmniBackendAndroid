package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.model.OmniUser
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.AuthUserAttribute

/**
 * Mapeia respostas do AWS Cognito e AWS Amplify para [OmniUser].
 */
internal object AmplifyUserMapper {

    /**
     * Mapeia um objeto [AuthUser] e atributos do AWS Cognito/Amplify para [OmniUser].
     *
     * @param user Usuário autenticado do Cognito.
     * @param attributes Lista opcional de atributos adicionais do usuário.
     * @return [OmniUser] normalizado.
     */
    fun toOmniUser(user: AuthUser, attributes: List<AuthUserAttribute> = emptyList()): OmniUser {
        val email = attributes.firstOrNull { it.key.keyString.equals("email", ignoreCase = true) }?.value
            ?: user.username
        val name = attributes.firstOrNull { it.key.keyString.equals("name", ignoreCase = true) }?.value
            ?: attributes.firstOrNull { it.key.keyString.equals("preferred_username", ignoreCase = true) }?.value
            ?: email.substringBefore("@")
        val picture = attributes.firstOrNull { it.key.keyString.equals("picture", ignoreCase = true) }?.value
        val verified = attributes.firstOrNull {
            it.key.keyString.equals("email_verified", ignoreCase = true)
        }?.value?.toBoolean() ?: false

        return OmniUser(
            uid = user.userId,
            email = email,
            displayName = name,
            photoUrl = picture,
            isEmailVerified = verified,
            isAnonymous = email.isBlank(),
        )
    }

    /**
     * Mapeia um mapa genérico de atributos de usuário do AWS Cognito/Amplify para [OmniUser].
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
            isAnonymous = email.isBlank(),
        )
    }
}
