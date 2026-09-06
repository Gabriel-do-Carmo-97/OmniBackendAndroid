package br.wgc.omnibackend.core.model.firestore

/**
 * Operadores lógicos e relacionais suportados para filtragem de coleções e consultas NoSQL.
 */
enum class OperatorType {
    /** Igualdade estrita (`field == value`). */
    EQUAL_TO,

    /** Desigualdade (`field != value`). */
    NOT_EQUAL_TO,

    /** Maior que (`field > value`). */
    GREATER_THAN,

    /** Menor que (`field < value`). */
    LESS_THAN,

    /** Maior ou igual (`field >= value`). */
    GREATER_THAN_OR_EQUAL_TO,

    /** Menor ou igual (`field <= value`). */
    LESS_THAN_OR_EQUAL_TO,

    /** Verifica se um array contém o elemento especificado. */
    ARRAY_CONTAINS,

    /** Verifica se um array contém qualquer um dos elementos fornecidos. */
    ARRAY_CONTAINS_ANY,

    /** Verifica se o campo está presente na lista de valores permitidos (`IN`). */
    IN,

    /** Verifica se o campo NÃO está presente na lista de valores (`NOT IN`). */
    NOT_IN
}
