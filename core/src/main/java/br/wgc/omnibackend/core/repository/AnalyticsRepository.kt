package br.wgc.omnibackend.core.repository

/**
 * Contrato agnóstico para eventos de telemetria, rastreamento de tela e analytics de produto.
 */
interface AnalyticsRepository {

    /**
     * Registra um evento de telemetria customizado com parâmetros adicionais.
     *
     * @param eventName Nome identificador do evento (ex: "purchase_completed", "button_click").
     * @param params Mapa de atributos e propriedades contextuais do evento, ou `null`.
     */
    fun logEvent(eventName: String, params: Map<String, Any>?)

    /**
     * Define uma propriedade de usuário persistente associada aos eventos futuros.
     *
     * @param name Nome identificador da propriedade (ex: "subscription_tier").
     * @param value Valor da propriedade (ex: "premium").
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Associa um identificador de usuário persistente à sessão de analytics.
     *
     * @param userId Identificador único do usuário.
     */
    fun setUserId(userId: String)

    /**
     * Rastreia a navegação ou visualização de uma tela pelo usuário.
     *
     * @param screenName Nome descritivo da tela (ex: "HomeScreen", "CheckoutScreen").
     * @param screenClass Nome da classe ou componente de tela (opcional).
     */
    fun trackScreenView(screenName: String, screenClass: String? = null)

    /**
     * Rastreia a abertura ou retomada do aplicativo em primeiro plano.
     */
    fun trackAppOpen()
}
