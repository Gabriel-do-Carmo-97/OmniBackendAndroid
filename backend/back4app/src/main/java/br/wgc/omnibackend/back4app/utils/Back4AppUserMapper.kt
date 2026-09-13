package br.wgc.omnibackend.back4app.utils

import br.wgc.omnibackend.core.model.OmniUser
import com.parse.ParseUser

/**
 * Converte o modelo de usuário do Parse Platform ([ParseUser]) para o modelo agnóstico do OmniBackend ([OmniUser]).
 */
internal object Back4AppUserMapper {

    /**
     * Mapeia um [ParseUser] para [OmniUser].
     *
     * @param parseUser Instância de [ParseUser] retornada pelo Parse SDK.
     * @return [OmniUser] correspondente.
     */
    fun toOmniUser(parseUser: ParseUser): OmniUser {
        val email = parseUser.email.orEmpty()
        val displayName = parseUser.getString("name") ?: parseUser.username.orEmpty()
        val photoUrl = parseUser.getParseFile("avatar")?.url
        val isVerified = parseUser.getBoolean("emailVerified")
        val isAnonymous = parseUser.username.orEmpty().startsWith("anon_") || email.isBlank()

        return OmniUser(
            uid = parseUser.objectId.orEmpty(),
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isEmailVerified = isVerified,
            isAnonymous = isAnonymous,
        )
    }
}
