package br.wgc.omnibackend.core.repository

import br.wgc.omnibackend.core.model.firestore.FilterRequest
import br.wgc.omnibackend.core.utils.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Contrato agnóstico para bancos de dados NoSQL orientados a documentos e coleções.
 *
 * Provê operações de CRUD tipadas, buscas parametrizadas e observação reativa de coleções
 * e documentos em tempo real.
 */
interface FirestoreRepository {

    /**
     * Adiciona ou cria um novo documento dentro de uma coleção.
     *
     * @param T Tipo do payload a ser persistido.
     * @param collection Nome da coleção de destino (ex: "users", "products").
     * @param data Objeto com os dados a serem salvos.
     * @param customId Identificador customizado opcional. Se `null`, um ID exclusivo é gerado automaticamente.
     * @return [DataResult.Success] com o ID gerado ou atribuído ao documento.
     */
    suspend fun <T : Any> addDocument(collection: String, data: T, customId: String? = null): DataResult<String>

    /**
     * Recupera um documento individual a partir do seu ID único.
     *
     * @param T Tipo para o qual o documento será desserializado.
     * @param collection Nome da coleção onde o documento reside.
     * @param documentId Identificador exclusivo do documento.
     * @param clazz Classe do tipo [T] para conversão reflexiva dos dados.
     * @return [DataResult.Success] contendo o objeto recuperado (ou `null` se não existir).
     */
    suspend fun <T : Any> getDocument(collection: String, documentId: String, clazz: Class<T>): DataResult<T?>

    /**
     * Atualiza campos pontuais de um documento existente sem sobrescrever os demais atributos.
     *
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento a ser alterado.
     * @param data Mapa chave-valor com os campos e novos valores a serem atualizados.
     * @return [DataResult.Success] caso a atualização seja confirmada.
     */
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): DataResult<Unit>

    /**
     * Remove permanentemente um documento de uma coleção.
     *
     * @param collection Nome da coleção.
     * @param documentId Identificador do documento a ser excluído.
     * @return [DataResult.Success] caso a exclusão seja realizada com sucesso.
     */
    suspend fun deleteDocument(collection: String, documentId: String): DataResult<Unit>

    /**
     * Executa uma consulta filtrada por múltiplos predicados em uma coleção.
     *
     * @param T Tipo para o qual os documentos retornados serão convertidos.
     * @param collection Nome da coleção.
     * @param filters Lista de filtros relacionais a serem aplicados ([FilterRequest]).
     * @param clazz Classe de destino para mapeamento dos registros.
     * @return [DataResult.Success] contendo a lista dos objetos correspondentes aos filtros.
     */
    suspend fun <T : Any> findDocuments(collection: String, filters: List<FilterRequest>, clazz: Class<T>): DataResult<List<T>>

    /**
     * Observa as modificações de um documento específico em tempo real.
     *
     * @param T Tipo de destino para conversão dos dados.
     * @param collection Nome da coleção.
     * @param documentId Identificador exclusivo do documento monitorado.
     * @param clazz Classe de destino para mapeamento.
     * @return [Flow] que emite atualizações contínuas do documento à medida que é modificado no servidor.
     */
    fun <T : Any> listenToDocument(collection: String, documentId: String, clazz: Class<T>): Flow<DataResult<T?>>

    /**
     * Observa as modificações dos resultados de uma consulta em tempo real.
     *
     * @param T Tipo de destino para conversão dos registros.
     * @param collection Nome da coleção.
     * @param filters Filtros de composição da consulta.
     * @param clazz Classe de destino para mapeamento.
     * @return [Flow] que emite a lista atualizada de registros sempre que a coleção é alterada.
     */
    fun <T : Any> listenToCollection(collection: String, filters: List<FilterRequest>, clazz: Class<T>): Flow<DataResult<List<T>>>
}
