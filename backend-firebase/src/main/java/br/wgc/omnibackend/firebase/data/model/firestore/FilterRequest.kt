package br.wgc.omnibackend.firebase.data.model.firestore

/**
 * Representa uma condição de filtro para ser usada em consultas ao Firestore.
 */
data class FilterRequest(
    val field: String,
    val operatorType: OperatorType,
    val value: Any
)

