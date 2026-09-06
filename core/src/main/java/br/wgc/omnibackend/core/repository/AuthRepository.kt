package br.wgc.omnibackend.core.repository

import android.net.Uri
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico e reativo para autenticação, controle de sessão e gerenciamento de identidade.
 *
 * Desacopla a camada de apresentação de fornecedores específicos de autenticação (Firebase Auth,
 * Supabase GoTrue, Appwrite Account).
 */
interface AuthRepository {

    /**
     * Fluxo reativo do estado da sessão do usuário em tempo real.
     * Emite o [OmniUser] atualmente logado ou `null` quando o usuário é desconectado.
     */
    val authState: Flow<OmniUser?>

    /**
     * Retorna de forma síncrona o usuário autenticado em memória, ou `null` se desconectado.
     */
    val currentUser: OmniUser?

    /**
     * Autentica o usuário no provedor utilizando e-mail e senha.
     *
     * @param email Endereço de e-mail cadastrado.
     * @param pass Senha secreta de acesso.
     * @return [DataResult.Success] com a entidade [OmniUser] se autenticado com sucesso,
     *         ou [DataResult.Failure] com [AppError.Auth] em caso de credenciais inválidas.
     */
    suspend fun login(email: String, pass: String): DataResult<OmniUser>

    /**
     * Registra uma nova conta de usuário utilizando e-mail e senha.
     *
     * @param email Endereço de e-mail para a nova conta.
     * @param pass Senha secreta a ser associada à conta.
     * @return [DataResult.Success] com a entidade [OmniUser] recém-criada,
     *         ou [DataResult.Failure] com [AppError.Auth.EmailAlreadyInUse] se o e-mail já existir.
     */
    suspend fun createUser(email: String, pass: String): DataResult<OmniUser>

    /**
     * Registra um novo usuário retornando a resposta detalhada de registro [RegisterUserResponse].
     *
     * @param email Endereço de e-mail para a nova conta.
     * @param pass Senha de acesso.
     * @return [DataResult.Success] contendo o resumo estruturado do cadastro efetuado.
     */
    suspend fun registerEmailWithPassword(email: String, pass: String): DataResult<RegisterUserResponse>

    /**
     * Dispara um e-mail de redefinição/recuperação de senha para o endereço informado.
     *
     * @param email Endereço de e-mail do destinatário da solicitação.
     * @return [DataResult.Success] com [Unit] caso o e-mail tenha sido enfileirado com sucesso.
     */
    suspend fun resetPassword(email: String): DataResult<Unit>

    /**
     * Envia um e-mail de verificação para o endereço associado ao usuário atualmente logado.
     *
     * @return [DataResult.Success] em caso de envio bem-sucedido.
     */
    suspend fun sendEmailVerification(): DataResult<Unit>

    /**
     * Atualiza a senha do usuário atualmente autenticado.
     *
     * @param newPassword Nova senha desejada.
     * @return [DataResult.Success] caso a senha tenha sido alterada com sucesso.
     */
    suspend fun updatePassword(newPassword: String): DataResult<Unit>

    /**
     * Atualiza o nome de exibição e/ou avatar do perfil do usuário autenticado.
     *
     * @param name Novo nome de exibição (opcional).
     * @param photoUri URI pública da nova foto de perfil (opcional).
     * @return [DataResult.Success] em caso de atualização bem-sucedida.
     */
    suspend fun updateProfile(name: String?, photoUri: Uri?): DataResult<Unit>

    /**
     * Atualiza o endereço de e-mail associado ao usuário atualmente autenticado.
     *
     * @param newEmail Novo endereço de e-mail.
     * @return [DataResult.Success] em caso de sucesso.
     */
    suspend fun updateEmail(newEmail: String): DataResult<Unit>

    /**
     * Reautentica o usuário com a senha atual para validação de operações de alta segurança.
     *
     * @param password Senha atual para confirmação de identidade.
     * @return [DataResult.Success] se a senha conferir com a conta ativa.
     */
    suspend fun reauthenticate(password: String): DataResult<Unit>

    /**
     * Autentica uma sessão anônima temporária (convidado).
     *
     * @return [DataResult.Success] com o identificador único gerado para a sessão anônima.
     */
    suspend fun loginAnonymously(): DataResult<String>

    /**
     * Verifica se existe um usuário autenticado ativo no momento.
     *
     * @return [DataResult.Success] com `true` se logado, ou `false` caso contrário.
     */
    suspend fun isUserLogged(): DataResult<Boolean>

    /**
     * Exclui permanentemente a conta do usuário atualmente autenticado no provedor.
     *
     * @return [DataResult.Success] se a conta foi removida com êxito.
     */
    suspend fun deleteUser(): DataResult<Unit>

    /**
     * Autentica o usuário utilizando um token de identidade do Google (Credential Manager).
     *
     * @param idToken Token de identidade emitido pela API de login do Google.
     * @return [DataResult.Success] com a entidade [OmniUser] do usuário autenticado.
     */
    suspend fun signInWithGoogle(idToken: String): DataResult<OmniUser>

    /**
     * Autentica o usuário via Passkeys ou Credential Manager API.
     *
     * @param credentialResponse Objeto de credencial retornado pelo Android Credential Manager.
     * @return [DataResult.Success] com o [OmniUser] autenticado.
     */
    suspend fun signInWithCredential(credentialResponse: Any): DataResult<OmniUser> {
        return DataResult.Failure(AppError.Auth.Generic(UnsupportedOperationException("Passkeys credential auth não implementado neste provedor")))
    }

    /**
     * Encerra a sessão ativa do usuário no dispositivo atual.
     *
     * @return [DataResult.Success] ao finalizar a sessão com sucesso.
     */
    suspend fun signOut(): DataResult<Unit>
}
