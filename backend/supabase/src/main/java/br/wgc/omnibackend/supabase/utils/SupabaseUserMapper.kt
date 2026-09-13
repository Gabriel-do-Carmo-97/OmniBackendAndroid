package br.wgc.omnibackend.supabase.utils

import br.wgc.omnibackend.core.model.OmniUser
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Utilitário responsável pela conversão do modelo de usuário do Supabase [UserInfo]
 * para o modelo canônico agnóstico do OmniBackend [OmniUser].
 */
internal object SupabaseUserMapper {

    /**
     * Converte [UserInfo] do Supabase GoTrue para [OmniUser].
     *
     * @param userInfo Entidade de usuário retornada pelo Supabase Auth.
     * @return Entidade [OmniUser] padronizada.
     */
    fun toOmniUser(userInfo: UserInfo): OmniUser {
        val metadata = userInfo.userMetadata
        val displayName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("user_name")?.jsonPrimitive?.contentOrNull

        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        val isAnonymous = metadata?.get("is_anonymous")?.jsonPrimitive?.booleanOrNull
            ?: userInfo.identities.isNullOrEmpty()

        return OmniUser(
            uid = userInfo.id,
            email = userInfo.email,
            displayName = displayName,
            photoUrl = avatarUrl,
            isEmailVerified = userInfo.emailConfirmedAt != null,
            isAnonymous = isAnonymous,
        )
    }
}
