package br.wgc.omnibackend.firebase.domain.usecase

import br.wgc.omnibackend.core.model.auth.NewUser
import br.wgc.omnibackend.core.model.auth.RegisterUserResponse
import br.wgc.omnibackend.core.model.auth.RegisteredUser
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult
import br.wgc.omnibackend.firebase.utils.toRegisteredUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Caso de uso para orquestração de registro de novos usuários.
 *
 * Cria a conta no provedor de autenticação, faz upload do avatar para o Cloud Storage (se fornecido),
 * persiste o perfil no Firestore e sincroniza o status de presença no Realtime Database.
 * Em caso de falha intermediária, desfaz as operações anteriores (rollback).
 *
 * @property auth Contrato de autenticação.
 * @property firestore Contrato do banco de dados de documentos.
 * @property database Contrato do banco de dados em tempo real.
 * @property storage Contrato do serviço de arquivos na nuvem.
 */
class UserRegisterUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val firestore: FirestoreRepository,
    private val database: RealtimeDatabaseRepository,
    private val storage: StorageRepository
) {

    /**
     * Executa o fluxo transacional de registro de usuário com suporte a rollback.
     *
     * @param newUser Dados do novo usuário fornecidos pela interface.
     * @param customBasePath Caminho base customizado para documentos no banco. Opcional.
     * @param updatePresence Se verdadeiro, ativa o status online em tempo real. Padrão: false.
     * @return [Flow] que emite [UseCaseResult] contendo a entidade [RegisteredUser] criada.
     */
    operator fun invoke(
        newUser: NewUser,
        customBasePath: String? = null,
        updatePresence: Boolean = false
    ): Flow<UseCaseResult<RegisteredUser>> = flow {
        emit(UseCaseResult.Loading)

        val authResult = auth.registerEmailWithPassword(
            newUser.email, newUser.password
        )

        val authResponse: RegisterUserResponse = when (authResult) {
            is DataResult.Success -> authResult.data
            is DataResult.Failure -> {
                emit(UseCaseResult.Failure(authResult.error))
                return@flow
            }
        }

        val basePath = customBasePath ?: if (newUser.isClient) "users/client" else "users/admin"
        val userPath = "$basePath/${authResponse.id}"
        val photoPath = "$userPath/photo"

        val photoUri = newUser.photo?.let { photoByteArray ->
            val uploadResult = storage.uploadFile(photoPath, photoByteArray.toByteArray()).first()
            when (uploadResult) {
                is DataResult.Success -> uploadResult.data
                is DataResult.Failure -> {
                    auth.deleteUser()
                    emit(UseCaseResult.Failure(uploadResult.error))
                    return@flow
                }
            }
        }

        val registeredUser = authResponse.toRegisteredUser(
            photo = photoUri,
            isClient = newUser.isClient
        )
        val firestoreResult = firestore.addDocument(userPath, registeredUser, registeredUser.id)
        if (firestoreResult is DataResult.Failure) {
            auth.deleteUser()
            if (photoUri != null) storage.delete(photoPath)
            emit(UseCaseResult.Failure(firestoreResult.error))
            return@flow
        }

        if (updatePresence) {
            val databaseResult = database.presence().goOnline(basePath, registeredUser.id)
            if (databaseResult is DataResult.Failure) {
                auth.deleteUser()
                if (photoUri != null) storage.delete(photoPath)
                firestore.deleteDocument(userPath, registeredUser.id)
                emit(UseCaseResult.Failure(databaseResult.error))
                return@flow
            }
        }

        emit(UseCaseResult.Success(registeredUser))
    }
}

