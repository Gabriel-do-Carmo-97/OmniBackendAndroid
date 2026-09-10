package br.wgc.omnibackend.core.di

import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository

/**
 * Seletor corporativo de backends para orquestração dinâmica em tempo de execução.
 *
 * Em arquiteturas híbridas (como `:bundle:hybrid`), permite que a aplicação cliente
 * determine dinamicamente qual implementação de [AuthRepository], [FirestoreRepository]
 * ou [StorageRepository] deve atender às injeções ativas sem quebra de contrato.
 */
object OmniBackendSelector {

    @Volatile
    private var customAuthRepository: AuthRepository? = null

    @Volatile
    private var customFirestoreRepository: FirestoreRepository? = null

    @Volatile
    private var customStorageRepository: StorageRepository? = null

    /**
     * Define o repositório de autenticação ativo.
     *
     * @param repository Implementação de [AuthRepository] a ser utilizada como padrão.
     */
    fun setActiveAuthRepository(repository: AuthRepository?) {
        customAuthRepository = repository
    }

    /**
     * Retorna o repositório de autenticação ativo configurado.
     */
    fun getActiveAuthRepository(): AuthRepository? = customAuthRepository

    /**
     * Define o repositório de banco de dados ativo.
     *
     * @param repository Implementação de [FirestoreRepository] a ser utilizada como padrão.
     */
    fun setActiveFirestoreRepository(repository: FirestoreRepository?) {
        customFirestoreRepository = repository
    }

    /**
     * Retorna o repositório de banco de dados ativo configurado.
     */
    fun getActiveFirestoreRepository(): FirestoreRepository? = customFirestoreRepository

    /**
     * Define o repositório de armazenamento de arquivos ativo.
     *
     * @param repository Implementação de [StorageRepository] a ser utilizada como padrão.
     */
    fun setActiveStorageRepository(repository: StorageRepository?) {
        customStorageRepository = repository
    }

    /**
     * Retorna o repositório de armazenamento ativo configurado.
     */
    fun getActiveStorageRepository(): StorageRepository? = customStorageRepository

    /**
     * Limpa todas as configurações ativas, restaurando o estado padrão.
     */
    fun reset() {
        customAuthRepository = null
        customFirestoreRepository = null
        customStorageRepository = null
    }
}
