package br.wgc.omnibackend.core.offline

/**
 * Operações suportadas em mutações pendentes de sincronização offline.
 */
enum class MutationType {
    ADD,
    UPDATE,
    DELETE,
}

/**
 * Item de mutação pendente gravado quando o dispositivo está sem conectividade de rede.
 */
data class PendingMutation(
    val id: String,
    val collection: String,
    val documentId: String,
    val type: MutationType,
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Contrato agnóstico da fila de mutações para sincronização offline.
 */
interface MutationQueueRepository {

    /** Enfileira uma nova mutação pendente. */
    suspend fun enqueue(mutation: PendingMutation)

    /** Retorna todas as mutações pendentes gravadas. */
    suspend fun getPendingMutations(): List<PendingMutation>

    /** Remove uma mutação processada da fila após sincronização bem-sucedida. */
    suspend fun remove(mutationId: String)

    /** Limpa todas as mutações pendentes. */
    suspend fun clearAll()
}
