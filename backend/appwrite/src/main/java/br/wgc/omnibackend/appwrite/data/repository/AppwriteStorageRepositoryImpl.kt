package br.wgc.omnibackend.appwrite.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.appwrite.utils.AppwriteErrorMapper
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.DataResult
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream

/**
 * Implementação concreta de [StorageRepository] utilizando o serviço [Storage] do Appwrite SDK.
 *
 * O parâmetro `path` é tratado como `bucketId/fileId` — o primeiro segmento antes de `/` é o
 * `bucketId` e o restante é o `fileId`. Caso não haja separador, usa [defaultBucketId].
 *
 * As URLs públicas seguem o padrão Appwrite REST:
 * `{endpoint}/storage/buckets/{bucketId}/files/{fileId}/view?project={projectId}`
 *
 * @param storage Serviço Storage do Appwrite SDK.
 * @param context Contexto Android para resolução de URIs locais.
 * @param endpoint URL base do Appwrite (ex: `https://cloud.appwrite.io/v1`).
 * @param projectId ID do projeto Appwrite.
 * @param defaultBucketId Bucket padrão utilizado quando o `path` não contém um prefixo de bucket.
 */
internal class AppwriteStorageRepositoryImpl(
    private val storage: Storage,
    private val context: Context,
    private val endpoint: String,
    private val projectId: String,
    private val defaultBucketId: String,
) : StorageRepository {

    // ─── Upload — Flow ────────────────────────────────────────────────────────

    /**
     * Faz upload de um array de bytes, emitindo o resultado via [Flow].
     *
     * @param path Caminho no formato `bucketId/fileId` ou apenas `fileId` (usa [defaultBucketId]).
     * @param fileData Conteúdo do arquivo em bytes.
     * @return [Flow] com [DataResult.Success] contendo a URL pública do arquivo.
     */
    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        emit(uploadBytesInternal(path, fileData))
    }

    /**
     * Faz upload a partir de uma [Uri] local, emitindo o resultado via [Flow].
     *
     * @param path Caminho de destino no Appwrite Storage.
     * @param fileUri URI local do arquivo.
     */
    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        emit(uploadUriInternal(path, fileUri))
    }

    /**
     * Faz upload a partir de um [InputStream], emitindo o resultado via [Flow].
     *
     * @param path Caminho de destino no Appwrite Storage.
     * @param inputStream Fluxo de bytes a ser enviado.
     */
    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val bytes = inputStream.use { it.readBytes() }
        emit(uploadBytesInternal(path, bytes))
    }

    // ─── Upload — Direct (suspend) ────────────────────────────────────────────

    /**
     * Faz upload direto (suspenso) de um array de bytes.
     *
     * @param path Caminho de destino no Appwrite Storage.
     * @param fileData Conteúdo do arquivo.
     */
    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> = uploadBytesInternal(path, fileData)

    /**
     * Faz upload direto (suspenso) a partir de uma [Uri] local.
     *
     * @param path Caminho de destino no Appwrite Storage.
     * @param fileUri URI local do arquivo.
     */
    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> = uploadUriInternal(path, fileUri)

    // ─── Download URL ─────────────────────────────────────────────────────────

    /**
     * Retorna a URL pública de visualização de um arquivo no Appwrite Storage.
     *
     * @param path Caminho do arquivo no formato `bucketId/fileId`.
     */
    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatchingStorage {
        val (bucketId, fileId) = parsePath(path)
        val url = buildViewUrl(bucketId, fileId)
        Uri.parse(url)
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    /**
     * Remove permanentemente um arquivo do Appwrite Storage.
     *
     * @param path Caminho do arquivo no formato `bucketId/fileId`.
     */
    override suspend fun delete(path: String): DataResult<Unit> = runCatchingStorage {
        val (bucketId, fileId) = parsePath(path)
        storage.deleteFile(bucketId, fileId)
        Unit
    }

    // ─── Internal Helpers ─────────────────────────────────────────────────────

    private suspend fun uploadBytesInternal(path: String, bytes: ByteArray): DataResult<Uri> = runCatchingStorage {
        val (bucketId, fileId) = parsePath(path)
        val inputFile = InputFile.fromBytes(bytes, fileId, "application/octet-stream")
        storage.createFile(bucketId, fileId, inputFile)
        Uri.parse(buildViewUrl(bucketId, fileId))
    }

    private suspend fun uploadUriInternal(path: String, fileUri: Uri): DataResult<Uri> = runCatchingStorage {
        val (bucketId, fileId) = parsePath(path)
        val bytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Não foi possível abrir o InputStream para URI: $fileUri")
        val inputFile = InputFile.fromBytes(bytes, fileId, "application/octet-stream")
        storage.createFile(bucketId, fileId, inputFile)
        Uri.parse(buildViewUrl(bucketId, fileId))
    }

    /**
     * Interpreta o `path` como `bucketId/fileId`.
     * Se não houver `/`, usa [defaultBucketId] e o path inteiro como fileId (gerado único se vazio).
     */
    private fun parsePath(path: String): Pair<String, String> {
        val slash = path.indexOf('/')
        return if (slash > 0) {
            path.substring(0, slash) to path.substring(slash + 1).let { it.ifBlank { ID.unique() } }
        } else {
            defaultBucketId to path.ifBlank { ID.unique() }
        }
    }

    /** Constrói a URL pública de visualização do arquivo no Appwrite. */
    private fun buildViewUrl(bucketId: String, fileId: String): String =
        "$endpoint/storage/buckets/$bucketId/files/$fileId/view?project=$projectId"

    private suspend inline fun <T> runCatchingStorage(crossinline block: suspend () -> T): DataResult<T> = try {
        DataResult.Success(block())
    } catch (e: AppwriteException) {
        DataResult.Failure(AppwriteErrorMapper.mapException(e, "storage"))
    } catch (e: Exception) {
        DataResult.Failure(AppwriteErrorMapper.mapThrowable(e, "storage"))
    }
}
