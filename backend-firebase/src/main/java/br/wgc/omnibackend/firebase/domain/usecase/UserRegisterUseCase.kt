package br.wgc.omnibackend.firebase.domain.usecase

import br.wgc.omnibackend.firebase.data.model.auth.RegisterUserResponse
import br.wgc.omnibackend.firebase.domain.model.NewUser
import br.wgc.omnibackend.firebase.domain.model.RegisteredUser
import br.wgc.omnibackend.firebase.domain.repository.AuthRepository
import br.wgc.omnibackend.firebase.domain.repository.FirestoreRepository
import br.wgc.omnibackend.firebase.domain.repository.RealtimeDatabaseRepository
import br.wgc.omnibackend.firebase.domain.repository.StorageRepository
import br.wgc.omnibackend.firebase.utils.DataResult
import br.wgc.omnibackend.firebase.utils.UseCaseResult
import br.wgc.omnibackend.firebase.utils.toRegisteredUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Caso de uso para executar a lógica de Registro de um usuário.
 * Recebe um modelo de domínio [NewUser] e retorna um Flow com o resultado contendo um [RegisteredUser].
 */
class UserRegisterUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val firestore: FirestoreRepository,
    private val database: RealtimeDatabaseRepository,
    private val storage: StorageRepository
) {

    /**
     * Permite que a classe seja chamada como uma função (ex: userRegisterUseCase(newUser)).
     *
     * @param newUser Dados do novo usuário
     * @param customBasePath Caminho customizado para o documento no Firestore/Database. Opcional.
     * @param updatePresence Se verdadeiro, marca presença online no Realtime Database. Padrão: false.
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
                    auth.delete()
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
            auth.delete()
            if (photoUri != null) storage.deleteFile(photoPath).first()
            emit(UseCaseResult.Failure(firestoreResult.error))
            return@flow
        }

        if (updatePresence) {
            val databaseResult = database.presence().goOnline(basePath, registeredUser.id)
            if (databaseResult is DataResult.Failure) {
                auth.delete()
                if (photoUri != null) storage.deleteFile(photoPath).first()
                firestore.deleteDocument(userPath, registeredUser.id)
                emit(UseCaseResult.Failure(databaseResult.error))
                return@flow
            }
        }

        emit(UseCaseResult.Success(registeredUser))
    }

}

