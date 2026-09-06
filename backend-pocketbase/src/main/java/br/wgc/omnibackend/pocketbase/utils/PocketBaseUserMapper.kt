package br.wgc.omnibackend.pocketbase.utils

import br.wgc.omnibackend.core.model.OmniUser

/**
 * Mapeia registros de usuário do PocketBase para [OmniUser].
 */
internal object PocketBaseUserMapper {

    /**
     * Mapeia um mapa de propriedades retornado pelo PocketBase (ex: de `/api/collections/users/records`) para [OmniUser].
     *
     * @param record Map com o payload do registro do PocketBase.
     * @param baseUrl URL base do PocketBase para construir a URL da foto de perfil.
     * @return [OmniUser] mapeado.
     */
    fun toOmniUser(record: Map<String, Any?>, baseUrl: String = ""): OmniUser {
        val id = record["id"]?.toString().orEmpty()
        val email = record["email"]?.toString().orEmpty()
        val name = record["name"]?.toString().orEmpty()
        val avatar = record["avatar"]?.toString().orEmpty()
        val verified = record["verified"] as? Boolean ?: false
        val collectionId = record["collectionId"]?.toString().orEmpty()

        val photoUrl = if (avatar.isNotBlank() && collectionId.isNotBlank() && id.isNotBlank()) {
            "$baseUrl/api/files/$collectionId/$id/$avatar"
        } else null

        return OmniUser(
            uid = id,
            email = email,
            displayName = name,
            photoUrl = photoUrl,
            isEmailVerified = verified,
            isAnonymous = email.isBlank()
        )
    }
}
