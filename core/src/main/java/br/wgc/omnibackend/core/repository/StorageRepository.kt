package br.wgc.omnibackend.core.repository

import android.net.Uri
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * Contrato agnóstico para armazenamento de arquivos e objetos na nuvem (Storage / Buckets).
 *
 * Provê upload via fluxos reativos ([Flow]) e suspensão direta ([suspend]), além de
 * download de URLs públicas e exclusão de artefatos.
 */
interface StorageRepository {

    /**
     * Realiza o upload de um array de bytes para o caminho especificado, emitindo o resultado via [Flow].
     *
     * @param path Caminho relativo de destino no bucket (ex: "avatars/user123.jpg").
     * @param fileData Array de bytes com o conteúdo do arquivo.
     * @return [Flow] que emite [DataResult.Success] com a [Uri] pública para download.
     */
    fun uploadFile(path: String, fileData: ByteArray): Flow<DataResult<Uri>>

    /**
     * Realiza o upload a partir de uma [Uri] local de arquivo, emitindo o resultado via [Flow].
     *
     * @param path Caminho relativo de destino no bucket.
     * @param fileUri URI do arquivo local a ser enviado.
     * @return [Flow] com o resultado contendo a [Uri] pública do arquivo publicado.
     */
    fun uploadFile(path: String, fileUri: Uri): Flow<DataResult<Uri>>

    /**
     * Realiza o upload a partir de uma stream de entrada [InputStream], emitindo via [Flow].
     *
     * @param path Caminho relativo de destino no bucket.
     * @param inputStream Fluxo de dados para leitura dos bytes.
     * @return [Flow] com o resultado contendo a [Uri] para download.
     */
    fun uploadFile(path: String, inputStream: InputStream): Flow<DataResult<Uri>>

    /**
     * Realiza o upload imediato e síncrono/suspenso de um array de bytes.
     *
     * @param path Caminho de destino no bucket.
     * @param fileData Array de bytes com o conteúdo.
     * @return [DataResult.Success] com a [Uri] de download ou [DataResult.Failure].
     */
    suspend fun uploadFileDirect(path: String, fileData: ByteArray): DataResult<Uri>

    /**
     * Realiza o upload imediato e síncrono/suspenso de um arquivo apontado por [Uri].
     *
     * @param path Caminho de destino no bucket.
     * @param fileUri URI local do arquivo a enviar.
     * @return [DataResult.Success] com a [Uri] de download ou [DataResult.Failure].
     */
    suspend fun uploadFileDirect(path: String, fileUri: Uri): DataResult<Uri>

    /**
     * Obtém a URL pública de download de um arquivo previamente armazenado no bucket.
     *
     * @param path Caminho do arquivo no bucket.
     * @return [DataResult.Success] com a [Uri] pública de acesso.
     */
    suspend fun getDownloadUrl(path: String): DataResult<Uri>

    /**
     * Remove permanentemente um arquivo ou objeto do bucket.
     *
     * @param path Caminho relativo do arquivo a ser deletado.
     * @return [DataResult.Success] com [Unit] após a exclusão ser confirmada pelo servidor.
     */
    suspend fun delete(path: String): DataResult<Unit>
}
