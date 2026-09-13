package br.wgc.omnibackend.core.model.firestore

/**
 * Encapsula uma cláusula de filtro para composição de consultas em bancos NoSQL orientados a documentos.
 *
 * Exemplo de uso:
 * ```kotlin
 * val filter = FilterRequest(
 *     field = "status",
 *     operatorType = OperatorType.EQUAL_TO,
 *     value = "ACTIVE"
 * )
 * ```
 *
 * @property field Caminho ou identificador da propriedade a ser filtrada (ex: "age", "profile.status").
 * @property operatorType O operador relacional a ser aplicado na filtragem ([OperatorType]).
 * @property value O valor literal ou lista de valores contra o qual a comparação será feita.
 */
data class FilterRequest(val field: String, val operatorType: OperatorType, val value: Any)
