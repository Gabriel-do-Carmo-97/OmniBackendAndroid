package br.wgc.omnibackend.supabase.data.repository

import android.content.Context
import android.net.Uri
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.supabase.utils.SupabaseErrorMapper
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Implementação corporativa do contrato [StorageRepository] utilizando o Supabase Storage (Buckets S3-compatible).
 *
 * Provê upload e download de arquivos em buckets do Supabase, resolução dinâmica de caminhos
 * e geração de URLs públicas.
 *
 * @property storage Módulo [Storage] do cliente Supabase.
 * @property context Contexto de aplicação opcional para leitura de [Uri]s locais via ContentResolver.
 * @property defaultBucket Nome do bucket padrão caso o caminho informado não inclua prefixo de bucket.
 */
class SupabaseStorageRepositoryImpl @Inject constructor(
    private val storage: Storage,
    private val context: Context? = null,
    private val defaultBucket: String = "public",
) : StorageRepository {

    /**
     * Realiza o upload de um array de bytes para o bucket indicado emitindo o resultado via [Flow].
     *
     * @param path Caminho do arquivo. Pode ter o formato "bucket/arquivo.ext" ou "arquivo.ext".
     * @param fileData Conteúdo binário do arquivo.
     */
    override fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileData))
    }

    /**
     * Realiza o upload a partir de uma [Uri] local de arquivo emitindo via [Flow].
     */
    override fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>> = flow {
        emit(uploadFileDirect(path, fileUri))
    }

    /**
     * Realiza o upload a partir de uma [InputStream] emitindo via [Flow].
     */
    override fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>> = flow {
        val result = runCatching {
            val bytes = inputStream.use { it.readBytes() }
            uploadFileDirect(path, bytes)
        }.getOrElse {
            DataResult.Failure(AppError.Storage.Generic(Exception("Falha na leitura da InputStream", it)))
        }
        emit(result)
    }

    /**
     * Realiza o upload imediato e suspenso de um array de bytes.
     *
     * @param path Caminho de destino no bucket.
     * @param fileData Conteúdo binário.
     * @return [DataResult.Success] contendo a [Uri] pública do arquivo publicado.
     */
    override suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri> = runCatching {
        val (bucket, relativePath) = resolveBucketAndPath(path)
        val bucketApi = storage.from(bucket)
        bucketApi.upload(relativePath, fileData) {
            upsert = true
        }
        val publicUrl = bucketApi.publicUrl(relativePath)
        DataResult.Success(Uri.parse(publicUrl))
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapStorageError(it))
    }

    /**
     * Realiza o upload imediato e suspenso a partir de uma [Uri] local do Android.
     */
    override suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri> = runCatching {
        val cr = context?.contentResolver
            ?: throw IllegalStateException(
                "Context é necessário para resolver URIs locais. Forneça o contexto em OmniSupabase.initialize().",
            )
        val bytes = cr.openInputStream(fileUri)?.use { it.readBytes() }
            ?: throw IOException("Falha ao abrir stream de leitura para a URI: $fileUri")
        uploadFileDirect(path, bytes)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapStorageError(it))
    }

    /**
     * Obtém a URL pública de download de um arquivo previamente armazenado.
     *
     * @param path Caminho do arquivo (ex: "avatars/foto.png" ou "meu-bucket/pasta/foto.png").
     */
    override suspend fun getDownloadUrl(path: String): DataResult<Uri> = runCatching {
        val (bucket, relativePath) = resolveBucketAndPath(path)
        val publicUrl = storage.from(bucket).publicUrl(relativePath)
        DataResult.Success(Uri.parse(publicUrl))
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapStorageError(it))
    }

    /**
     * Remove permanentemente um arquivo ou objeto do bucket.
     *
     * @param path Caminho do arquivo no bucket.
     */
    override suspend fun delete(path: String): DataResult<Unit> = runCatching {
        val (bucket, relativePath) = resolveBucketAndPath(path)
        storage.from(bucket).delete(listOf(relativePath))
        DataResult.Success(Unit)
    }.getOrElse {
        DataResult.Failure(SupabaseErrorMapper.mapStorageError(it))
    }

    private fun resolveBucketAndPath(fullPath: String): Pair<String, String> {
        val clean = fullPath.trimStart('/')
        return if (clean.contains('/')) {
            val bucket = clean.substringBefore('/')
            val relativePath = clean.substringAfter('/')
            bucket to relativePath
        } else {
            defaultBucket to clean
        }
    }
}
