package br.wgc.omnibackend.appwrite

import android.content.Context
import br.wgc.omnibackend.appwrite.data.repository.AppwriteAuthRepositoryImpl
import br.wgc.omnibackend.appwrite.data.repository.AppwriteDatabaseRepositoryImpl
import br.wgc.omnibackend.appwrite.data.repository.AppwriteStorageRepositoryImpl
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import com.google.gson.Gson
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.appwrite.services.Storage

/**
 * Ponto de entrada e Fachada corporativa do driver Appwrite para o OmniBackend Android.
 *
 * Fornece acesso thread-safe e lazy-initialized às implementações concretas dos contratos
 * agnósticos do módulo `:core` utilizando o Appwrite Android SDK (`Account`, `Databases`, `Storage`).
 *
 * Exemplo de uso:
 * ```kotlin
 * // No Application.onCreate():
 * OmniAppwrite.initialize(
 *     context = this,
 *     endpoint = "https://cloud.appwrite.io/v1",
 *     projectId = "seu-project-id",
 *     databaseId = "seu-database-id",
 *     defaultBucketId = "seu-bucket-id"
 * )
 *
 * // Nos ViewModels ou UseCases:
 * val auth = OmniAppwrite.auth
 * val db = OmniAppwrite.database
 * val storage = OmniAppwrite.storage
 * ```
 */
object OmniAppwrite {

    @Volatile
    private var _initialized = false

    private var _endpoint: String = ""
    private var _projectId: String = ""
    private var _databaseId: String = ""
    private var _defaultBucketId: String = ""
    private var _context: Context? = null

    // SDK Client is created once during initialize()
    private var _client: Client? = null

    // Lazy service accessors
    private val sdkClient: Client
        get() = _client ?: error("OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)")

    private val account: Account by lazy { Account(sdkClient) }
    private val databases: Databases by lazy { Databases(sdkClient) }
    private val storageService: Storage by lazy { Storage(sdkClient) }
    private val realtime: Realtime by lazy { Realtime(sdkClient) }

    /**
     * Inicializa a configuração do cliente Appwrite.
     *
     * Deve ser chamado antes de qualquer acesso a [auth], [database] ou [storage].
     * Chamadas subsequentes após a inicialização são ignoradas (double-checked locking).
     *
     * @param context Contexto da aplicação Android (preferencialmente `applicationContext`).
     * @param endpoint URL do endpoint da API do Appwrite (ex: "https://cloud.appwrite.io/v1").
     * @param projectId Identificador único do projeto no Appwrite.
     * @param databaseId Identificador do banco de dados Appwrite a ser utilizado nas operações de [database].
     * @param defaultBucketId Bucket padrão para operações de [storage] quando o path não inclui `bucketId/`.
     */
    fun initialize(
        context: Context,
        endpoint: String,
        projectId: String,
        databaseId: String,
        defaultBucketId: String = "default"
    ) {
        if (!_initialized) {
            synchronized(this) {
                if (!_initialized) {
                    _context = context.applicationContext
                    _endpoint = endpoint
                    _projectId = projectId
                    _databaseId = databaseId
                    _defaultBucketId = defaultBucketId
                    _client = Client(context.applicationContext)
                        .setEndpoint(endpoint)
                        .setProject(projectId)
                    _initialized = true
                }
            }
        }
    }

    /** Retorna `true` caso o SDK já tenha sido inicializado. */
    val initialized: Boolean get() = _initialized

    /**
     * Endpoint configurado da API do Appwrite.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val endpoint: String
        get() {
            check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)" }
            return _endpoint
        }

    /**
     * Project ID configurado do Appwrite.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val projectId: String
        get() {
            check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)" }
            return _projectId
        }

    /**
     * Database ID configurado do Appwrite.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val databaseId: String
        get() {
            check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso. Chame OmniAppwrite.initialize(...)" }
            return _databaseId
        }

    /**
     * Implementação de [AuthRepository] para autenticação de usuários via Appwrite Account API.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val auth: AuthRepository by lazy {
        check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso." }
        AppwriteAuthRepositoryImpl(account)
    }

    /**
     * Implementação de [FirestoreRepository] para persistência de documentos via Appwrite Databases API.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val database: FirestoreRepository by lazy {
        check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso." }
        AppwriteDatabaseRepositoryImpl(databases, realtime, _databaseId, Gson())
    }

    /**
     * Implementação de [StorageRepository] para armazenamento de arquivos via Appwrite Storage API.
     *
     * @throws IllegalStateException se [initialize] ainda não foi invocado.
     */
    val storage: StorageRepository by lazy {
        check(_initialized) { "OmniAppwrite deve ser inicializado antes do uso." }
        AppwriteStorageRepositoryImpl(
            storage = storageService,
            context = _context ?: error("OmniAppwrite deve ser inicializado antes do uso."),
            endpoint = _endpoint,
            projectId = _projectId,
            defaultBucketId = _defaultBucketId
        )
    }

    /**
     * Reinicia o estado interno do singleton (útil apenas em testes unitários).
     *
     * **NÃO** deve ser chamado em produção.
     */
    @Suppress("unused")
    internal fun resetForTesting() {
        synchronized(this) {
            _initialized = false
            _client = null
            _context = null
            _endpoint = ""
            _projectId = ""
            _databaseId = ""
            _defaultBucketId = ""
        }
    }
}
