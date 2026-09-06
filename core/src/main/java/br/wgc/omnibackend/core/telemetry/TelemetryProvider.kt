package br.wgc.omnibackend.core.telemetry

import br.wgc.omnibackend.core.utils.AppError

/**
 * Contrato agnóstico de observabilidade, captura de exceções e monitoramento de falhas em produção.
 *
 * Provê abstração unificada para ferramentas de Crash Reporting e APM (como Firebase Crashlytics,
 * Sentry, Datadog ou ferramentas proprietárias).
 */
interface TelemetryProvider {

    /**
     * Registra um erro de negócio estruturado pertencente à hierarquia [AppError].
     *
     * @param error A falha estruturada ocorrida.
     * @param attributes Metadados e contexto adicional da falha.
     */
    fun recordError(error: AppError, attributes: Map<String, Any> = emptyMap())

    /**
     * Registra uma exceção de sistema ou falha não tratada [Throwable].
     *
     * @param throwable A exceção ou erro capturado.
     * @param attributes Mapa de contexto e atributos chave-valor para diagnóstico.
     */
    fun recordException(throwable: Throwable, attributes: Map<String, Any> = emptyMap())

    /**
     * Adiciona uma mensagem de log aos trilhos de rastreamento (breadcrumbs) da sessão.
     *
     * @param message Mensagem de texto descritiva da ação realizada.
     */
    fun log(message: String)

    /**
     * Associa o identificador do usuário às falhas e relatórios de diagnóstico gerados.
     *
     * @param userId Identificador único do usuário logado.
     */
    fun setUserId(userId: String)

    /**
     * Define um atributo ou metadado personalizado na sessão de telemetria.
     *
     * @param key Chave de identificação do atributo.
     * @param value Valor do atributo (número, texto, booleano).
     */
    fun setAttribute(key: String, value: Any)
}
