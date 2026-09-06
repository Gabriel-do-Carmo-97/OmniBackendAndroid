package br.wgc.omnibackend.core.model

/**
 * Representação agnóstica de um usuário autenticado no OmniBackend.
 * Independe de Firebase, Supabase ou outro provedor.
 */
data class OmniUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isAnonymous: Boolean = false
)
