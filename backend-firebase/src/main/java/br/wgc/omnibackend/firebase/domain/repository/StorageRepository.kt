package br.wgc.omnibackend.firebase.domain.repository

import android.net.Uri
import br.wgc.omnibackend.firebase.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Um repositório genérico para interagir com o Firebase Storage.
 * Ele fornece métodos para fazer upload, download e excluir arquivos, permitindo o gerenciamento
 * flexível de vários tipos de conteúdo, como imagens de produtos, perfis de usuário, etc.
 */
interface StorageRepository {

    /**
     * Faz o upload de um arquivo para um caminho especificado no Firebase Storage usando ByteArray.
     *
     * @param path O caminho completo no Storage onde o arquivo será salvo (ex: "images/users/user123.jpg").
     * @param fileData Um ByteArray contendo os dados do arquivo.
     * @return Um [Flow] que emite um [DataResult] com a [Uri] de download em caso de sucesso, ou um erro em caso de falha.
     */
    fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>>

    /**
     * Faz upload direto a partir de uma [Uri] local (arquivo/mídia no dispositivo), prevenindo OOM.
     */
    suspend fun uploadFile(path: String, fileUri: Uri): DataResult<Uri>

    /**
     * Faz upload a partir de um [java.io.InputStream], transmitindo em streaming sem carregar tudo em memória.
     */
    suspend fun uploadFile(path: String, inputStream: java.io.InputStream): DataResult<Uri>

    /**
     * Recupera a URL de download pública para um único arquivo do Storage de forma síncrona/suspend.
     */
    suspend fun getDownloadUrl(path: String): DataResult<Uri>

    /**
     * Recupera a URL de download pública para um único arquivo do Storage como Flow.
     *
     * @param path O caminho completo do arquivo no Storage.
     * @return Um [Flow] que emite um [DataResult] com a [Uri] de download em caso de sucesso, ou um erro em caso de falha.
     */
    fun getFileUrl(path: String): Flow<DataResult<Uri>>

    /**
     * Recupera uma lista de URLs de download públicas para todos os arquivos dentro de uma "pasta" especificada no Storage.
     *
     * @param folderPath O caminho da pasta no Storage (ex: "images/products/product123/").
     * @return Um [Flow] que emite um [DataResult] com a lista de [Uri]s em caso de sucesso, ou um erro em caso de falha.
     */
    fun listFiles(folderPath: String): Flow<DataResult<List<Uri>>>

    /**
     * Exclui um arquivo do Firebase Storage (suspend).
     */
    suspend fun delete(path: String): DataResult<Unit>

    /**
     * Exclui um arquivo do Firebase Storage.
     *
     * @param path O caminho completo do arquivo a ser excluído.
     * @return Um [Flow] que emite um [DataResult] com [Unit] em caso de sucesso, ou um erro em caso de falha.
     */
    fun deleteFile(path: String): Flow<DataResult<Unit>>
}

