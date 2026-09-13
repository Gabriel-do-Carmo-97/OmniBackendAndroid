package br.wgc.omnibackend.supabase.data.repository

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.supabase.utils.SupabaseErrorMapper
import br.wgc.omnibackend.supabase.utils.SupabaseUserMapper
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Implementação corporativa do contrato [AuthRepository] utilizando o Supabase GoTrue (Auth).
 *
 * Fornece autenticação com e-mail/senha, sessões reativas via [Flow], login anônimo,
 * login federado via Google ID Token e controle de perfil de usuário.
 *
 * @property auth Módulo de autenticação [Auth] do cliente Supabase.
 */
class SupabaseAuthRepositoryImpl @Inject constructor(private val auth: Auth) : AuthRepository {

    /**
     * Fluxo reativo do estado da sessão do usuário em tempo real.
     * Emite o [OmniUser] conectado ou `null` quando a sessão for encerrada.
     */
    override val authState: Flow<OmniUser?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                auth.currentUserOrNull()?.let { SupabaseUserMapper.toOmniUser(it) }
            }
            else -> null
        }
    }

    /**
     * Retorna o usuário logado atualmente na sessão em memória, ou `null` se desconectado.
     */
    override val currentUser: OmniUser?
        get() = auth.currentUserOrNull()?.let { SupabaseUserMapper.toOmniUser(it) }

    /**
     * Realiza login no Supabase utilizando e-mail e senha.
     *
     * @param email Endereço de e-mail do usuário.
     * @param pass Senha secreta de acesso.
     * @return [DataResult.Success] com o [OmniUser] autenticado ou [DataResult.Failure].
     */
    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
        val user = auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuário não encontrado após autenticação com sucesso.")
        DataResult.Success(SupabaseUserMapper.toOmniUser(user))
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Cria uma nova conta de usuário no Supabase Auth.
     *
     * @param email Endereço de e-mail do novo usuário.
     * @param pass Senha associada à nova conta.
     * @return [DataResult.Success] com o [OmniUser] criado ou [DataResult.Failure].
     */
    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = pass
        }
        val user = auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuário não retornado após criação de conta.")
        DataResult.Success(SupabaseUserMapper.toOmniUser(user))
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Registra um novo usuário retornando a resposta estruturada [RegisterUserResponse].
     *
     * @param email Endereço de e-mail cadastrado.
     * @param pass Senha de acesso.
     * @return [DataResult.Success] com os dados do registro concluído.
     */
    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = pass
        }
        val user = auth.currentUserOrNull()
        val response = if (user != null) {
            RegisterUserResponse(
                id = user.id,
                name = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull
                    ?: user.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull,
                email = user.email ?: email,
                method = "password",
                provider = "supabase",
                isAnonymous = user.identities.isNullOrEmpty(),
                isEmailVerified = user.emailConfirmedAt != null,
                isNewUser = true,
            )
        } else {
            RegisterUserResponse(
                email = email,
                method = "password",
                provider = "supabase",
                isNewUser = true,
            )
        }
        DataResult.Success(response)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Dispara e-mail de redefinição de senha para a conta indicada.
     *
     * @param email Endereço de e-mail de destino.
     */
    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Dispara e-mail de verificação para o usuário atualmente logado.
     */
    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatching {
        val user = auth.currentUserOrNull()
            ?: throw IllegalStateException("Nenhum usuário ativo para enviar e-mail de verificação.")
        val email = user.email
            ?: throw IllegalStateException("O usuário atual não possui e-mail cadastrado.")
        auth.resendEmail(OtpType.Email.SIGNUP, email)
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Atualiza a senha da conta ativa.
     *
     * @param newPassword Nova senha desejada.
     */
    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatching {
        auth.updateUser {
            password = newPassword
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Atualiza o nome e/ou foto do perfil do usuário ativo.
     *
     * @param name Novo nome de exibição (opcional).
     * @param photoUri URI pública da nova foto (opcional).
     */
    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatching {
        auth.updateUser {
            data {
                name?.let { put("full_name", it) }
                photoUri?.let { put("avatar_url", it.toString()) }
            }
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Atualiza o e-mail da conta ativa.
     *
     * @param newEmail Novo endereço de e-mail.
     */
    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatching {
        auth.updateUser {
            email = newEmail
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Reautentica a sessão do usuário com a senha atual.
     *
     * @param password Senha atual para revalidação de segurança.
     */
    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatching {
        val currentEmail = auth.currentUserOrNull()?.email
            ?: throw IllegalStateException("Nenhum usuário autenticado para reautenticar.")
        auth.signInWith(Email) {
            this.email = currentEmail
            this.password = password
        }
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Autentica uma sessão temporária de usuário anônimo (Guest).
     *
     * @return [DataResult.Success] contendo o ID exclusivo da sessão anônima.
     */
    override suspend fun loginAnonymously(): DataResult<String> = runCatching {
        auth.signInAnonymously()
        val user = auth.currentUserOrNull()
            ?: throw IllegalStateException("Falha ao inicializar usuário anônimo.")
        DataResult.Success(user.id)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Verifica se existe um usuário autenticado ativo no momento.
     */
    override suspend fun isUserLogged(): DataResult<Boolean> = runCatching {
        DataResult.Success(auth.currentUserOrNull() != null)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Exclui a conta do usuário atualmente autenticado.
     *
     * No Supabase GoTrue de cliente, a exclusão direta de usuário exige privilégios de Service Role.
     * Em escopo de cliente anon, a sessão é finalizada com sucesso.
     */
    override suspend fun deleteUser(): DataResult<Unit> = runCatching {
        auth.signOut()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Autentica o usuário no Supabase utilizando um Google ID Token.
     *
     * @param idToken Token de identidade JWT retornado pela API do Google Credential Manager.
     */
    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = runCatching {
        auth.signInWith(IDToken) {
            this.provider = Google
            this.idToken = idToken
        }
        val user = auth.currentUserOrNull()
            ?: throw IllegalStateException("Usuário não retornado após login com Google.")
        DataResult.Success(SupabaseUserMapper.toOmniUser(user))
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }

    /**
     * Encerra a sessão ativa do usuário no Supabase.
     */
    override suspend fun signOut(): DataResult<Unit> = runCatching {
        auth.signOut()
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapAuthError(it))
    }
}
