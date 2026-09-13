package br.wgc.omnibackend.appwrite.utils

import br.wgc.omnibackend.core.model.OmniUser
import io.appwrite.models.User

/**
 * Responsável por converter o modelo de usuário do Appwrite SDK ([User]) no modelo agnóstico
 * do OmniBackend ([OmniUser]).
 *
 * Garante que nenhuma classe do SDK Appwrite vaze além do módulo `:backend-appwrite`.
 */
internal object AppwriteUserMapper {

    /**
     * Converte um [User] do Appwrite SDK em [OmniUser].
     *
     * - `uid` → [User.id]
     * - `email` → [User.email] (pode ser vazio para sessões anônimas)
     * - `displayName` → [User.name]
     * - `photoUrl` → `null` (Appwrite não fornece URL de foto no modelo de usuário)
     * - `isEmailVerified` → [User.emailVerification]
     * - `isAnonymous` → derivado de `email` vazio e `name` vazio (sessão anônima não possui dados de identidade)
     *
     * @param user Usuário retornado pelo SDK Appwrite.
     * @return [OmniUser] com os dados mapeados.
     */
    fun toOmniUser(user: User<Map<String, Any>>): OmniUser {
        val anonymous = user.email.isNullOrBlank() && user.name.isNullOrBlank()
        return OmniUser(
            uid = user.id,
            email = user.email.orEmpty(),
            displayName = user.name.orEmpty(),
            photoUrl = null,
            isEmailVerified = user.emailVerification,
            isAnonymous = anonymous,
        )
    }
}
