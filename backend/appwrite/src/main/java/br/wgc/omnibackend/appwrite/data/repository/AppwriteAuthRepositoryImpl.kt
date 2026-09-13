package br.wgc.omnibackend.appwrite.data.repository

import android.net.Uri
import br.wgc.omnibackend.appwrite.utils.AppwriteErrorMapper
import br.wgc.omnibackend.appwrite.utils.AppwriteUserMapper
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementação concreta de [AuthRepository] utilizando o Appwrite SDK ([Account]).
 *
 * Realiza autenticação por e-mail/senha, sessão anônima, redefinição de senha, verificação de
 * e-mail, atualização de perfil e controle de estado de sessão via [authState].
 *
 * Toda exceção do SDK ([AppwriteException]) é mapeada para [AppError] antes de ser emitida,
 * garantindo zero vazamento de tipos Appwrite além deste módulo.
 *
 * @param account Serviço de conta do Appwrite SDK.
 */
internal class AppwriteAuthRepositoryImpl(private val account: Account) : AuthRepository {

    @Volatile
    private var cachedUser: OmniUser? = null

    /**
     * Fluxo reativo do estado da sessão. Emite o usuário atual via polling one-shot ao coletar,
     * pois o Appwrite Android SDK não fornece listener nativo de mudança de sessão.
     * Para um listener contínuo, use [io.appwrite.services.Realtime] externo ao módulo.
     */
    override val authState: Flow<OmniUser?>
        get() = callbackFlow {
            try {
                val user = account.get()
                val omniUser = AppwriteUserMapper.toOmniUser(user)
                cachedUser = omniUser
                trySend(omniUser)
            } catch (e: AppwriteException) {
                cachedUser = null
                trySend(null)
            }
            awaitClose()
        }

    /** Retorna o usuário autenticado armazenado em cache no último evento de sessão. */
    override val currentUser: OmniUser?
        get() = cachedUser

    // ─── Login ────────────────────────────────────────────────────────────────

    /**
     * Autentica o usuário por e-mail e senha.
     *
     * @param email Endereço de e-mail cadastrado.
     * @param pass Senha de acesso.
     * @return [DataResult.Success] com [OmniUser] ou [DataResult.Failure] com [AppError.Auth].
     */
    override suspend fun login(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        account.createEmailPasswordSession(email, pass)
        val user = account.get()
        val omniUser = AppwriteUserMapper.toOmniUser(user)
        cachedUser = omniUser
        omniUser
    }

    // ─── Create / Register ────────────────────────────────────────────────────

    /**
     * Cria um novo usuário com e-mail e senha e retorna o [OmniUser] criado.
     *
     * @param email E-mail para a nova conta.
     * @param pass Senha da nova conta.
     */
    override suspend fun createUser(email: String, pass: String): DataResult<OmniUser> = runCatchingAuth {
        val created = account.create(ID.unique(), email, pass)
        val omniUser = AppwriteUserMapper.toOmniUser(created)
        cachedUser = omniUser
        omniUser
    }

    /**
     * Registra um novo usuário e retorna um resumo estruturado [RegisterUserResponse].
     *
     * @param email E-mail para a nova conta.
     * @param pass Senha da nova conta.
     */
    override suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse> = runCatchingAuth {
        val created = account.create(ID.unique(), email, pass)
        RegisterUserResponse(
            id = created.id,
            name = created.name.takeIf { it.isNotBlank() },
            email = created.email,
            method = "email",
            provider = "appwrite",
            isAnonymous = false,
            isEmailVerified = created.emailVerification,
            isNewUser = true,
        )
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    /**
     * Envia um e-mail de redefinição de senha para o endereço informado.
     *
     * @param email Endereço do destinatário.
     */
    override suspend fun resetPassword(email: String): DataResult<Unit> = runCatchingAuth {
        // Appwrite createRecovery requires a redirect URL — use a placeholder deep-link
        account.createRecovery(email, "omnibackend://reset-password")
        Unit
    }

    /**
     * Atualiza a senha do usuário autenticado.
     *
     * @param newPassword Nova senha desejada.
     */
    override suspend fun updatePassword(newPassword: String): DataResult<Unit> = runCatchingAuth {
        account.updatePassword(newPassword)
        Unit
    }

    // ─── Email Verification ────────────────────────────────────────────────────

    /**
     * Envia um e-mail de verificação para o usuário autenticado.
     */
    override suspend fun sendEmailVerification(): DataResult<Unit> = runCatchingAuth {
        account.createEmailVerification("omnibackend://verify-email")
        Unit
    }

    // ─── Profile ──────────────────────────────────────────────────────────────

    /**
     * Atualiza o nome de exibição do perfil. O Appwrite SDK não armazena foto diretamente
     * no `Account` — photoUri é ignorado (deve ser salvo via Storage + Databases).
     *
     * @param name Novo nome de exibição (opcional).
     * @param photoUri URI da foto (não gerenciado pelo Account Appwrite).
     */
    override suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit> = runCatchingAuth {
        if (name != null) {
            account.updateName(name)
        }
        Unit
    }

    /**
     * Atualiza o e-mail do usuário autenticado.
     *
     * @param newEmail Novo endereço de e-mail.
     */
    override suspend fun updateEmail(newEmail: String): DataResult<Unit> = runCatchingAuth {
        account.updateEmail(newEmail, "")
        Unit
    }

    // ─── Reauthenticate ────────────────────────────────────────────────────────

    /**
     * Reautentica o usuário com a senha atual. No Appwrite, consiste em criar uma nova sessão.
     *
     * @param password Senha atual para confirmação.
     */
    override suspend fun reauthenticate(password: String): DataResult<Unit> = runCatchingAuth {
        val user = account.get()
        account.createEmailPasswordSession(user.email, password)
        Unit
    }

    // ─── Anonymous ────────────────────────────────────────────────────────────

    /**
     * Inicia uma sessão anônima e retorna o ID do usuário criado.
     */
    override suspend fun loginAnonymously(): DataResult<String> = runCatchingAuth {
        val session = account.createAnonymousSession()
        session.userId
    }

    // ─── Session State ────────────────────────────────────────────────────────

    /**
     * Verifica se há um usuário autenticado ativo.
     */
    override suspend fun isUserLogged(): DataResult<Boolean> = runCatchingAuth {
        try {
            account.get()
            true
        } catch (e: AppwriteException) {
            false
        }
    }

    // ─── Delete User ─────────────────────────────────────────────────────────

    /**
     * Exclui a sessão atual (Appwrite não permite deleção da conta via Account SDK no cliente).
     */
    override suspend fun deleteUser(): DataResult<Unit> = runCatchingAuth {
        account.deleteSession("current")
        cachedUser = null
        Unit
    }

    // ─── OAuth / Google ───────────────────────────────────────────────────────

    /**
     * Autentica com um token de identidade Google. O Appwrite não suporta diretamente
     * `signInWithCredential(idToken)` no SDK Android — esta implementação requer um fluxo OAuth2
     * via Activity. Retorna [AppError.Auth.Generic] indicando a limitação da plataforma.
     *
     * @param idToken Token de identidade Google (não suportado diretamente pelo SDK Appwrite Android).
     */
    override suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser> = DataResult.Failure(
        AppError.Auth.Generic(
            IllegalStateException(
                "Appwrite Google Sign-In requer fluxo OAuth2 via Activity. " +
                    "Use Account.createOAuth2Session(activity, OAuthProvider.GOOGLE) diretamente.",
            ),
        ),
    )

    // ─── Sign Out ─────────────────────────────────────────────────────────────

    /**
     * Encerra a sessão ativa do usuário no dispositivo.
     */
    override suspend fun signOut(): DataResult<Unit> = runCatchingAuth {
        account.deleteSession("current")
        cachedUser = null
        Unit
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private suspend inline fun <T> runCatchingAuth(crossinline block: suspend () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: AppwriteException) {
        DataResult.Failure(AppwriteErrorMapper.mapException(e, "auth"))
    } catch (e: Exception) {
        DataResult.Failure(AppwriteErrorMapper.mapThrowable(e, "auth"))
    }
}
