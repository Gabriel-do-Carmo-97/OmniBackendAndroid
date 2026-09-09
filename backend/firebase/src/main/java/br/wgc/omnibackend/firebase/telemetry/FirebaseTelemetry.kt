package br.wgc.omnibackend.firebase.telemetry

import br.wgc.omnibackend.core.telemetry.TelemetryProvider
import br.wgc.omnibackend.core.utils.AppError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do provedor de telemetria [TelemetryProvider] conectada ao Firebase Crashlytics e Performance.
 *
 * Padroniza a captura estruturada de falhas [AppError], exceções de sistema e métricas de desempenho.
 *
 * @property crashlytics Instância do Firebase Crashlytics.
 * @property performance Instância do Firebase Performance Monitoring.
 */
@Singleton
class FirebaseTelemetry @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
    @PublishedApi internal val performance: FirebasePerformance
) : TelemetryProvider {

    /**
     * Registra um erro de domínio [AppError] no Firebase Crashlytics como evento não-fatal.
     *
     * @param error O erro estruturado a registrar.
     * @param attributes Atributos contextuais adicionais.
     */
    override fun recordError(error: AppError, attributes: Map<String, Any>) {
        attributes.forEach { (key, value) -> setAttribute(key, value) }
        when (error) {
            is AppError.Generic.Unknown -> {
                crashlytics.recordException(error.throwable)
            }
            is AppError.Auth.Generic -> {
                crashlytics.recordException(error.exception)
            }
            else -> {
                crashlytics.log("NonFatal AppError: ${error.javaClass.simpleName}")
                crashlytics.recordException(AppErrorException(error.toString()))
            }
        }
    }

    /**
     * Registra uma exceção de sistema ou falha não tratada no Crashlytics.
     */
    override fun recordException(throwable: Throwable, attributes: Map<String, Any>) {
        attributes.forEach { (key, value) -> setAttribute(key, value) }
        crashlytics.recordException(throwable)
    }

    /**
     * Registra uma mensagem nos breadcrumbs da sessão do Crashlytics.
     */
    override fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Associa o identificador do usuário logado à sessão de Crashlytics.
     */
    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Define um atributo customizado chave-valor no Crashlytics.
     */
    override fun setAttribute(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }

    /**
     * Mede o tempo de execução de um bloco síncrono utilizando o Firebase Performance.
     *
     * @param traceName Nome identificador da métrica de rastreio.
     * @param block Bloco a executar.
     */
    inline fun <T> trace(traceName: String, block: (Trace) -> T): T {
        val trace = performance.newTrace(traceName)
        trace.start()
        return try {
            block(trace)
        } finally {
            trace.stop()
        }
    }

    /**
     * Mede o tempo de execução de um bloco de corrotina suspensa via Firebase Performance.
     *
     * @param traceName Nome da métrica.
     * @param block Bloco assíncrono suspenso.
     */
    suspend inline fun <T> traceAsync(traceName: String, crossinline block: suspend (Trace) -> T): T {
        val trace = performance.newTrace(traceName)
        trace.start()
        return try {
            block(trace)
        } finally {
            trace.stop()
        }
    }

    companion object {
        @Volatile
        private var instance: FirebaseTelemetry? = null

        /**
         * Retorna a instância singleton padrão de [FirebaseTelemetry].
         */
        fun get(): FirebaseTelemetry =
            instance ?: synchronized(this) {
                instance ?: FirebaseTelemetry(
                    FirebaseCrashlytics.getInstance(),
                    FirebasePerformance.getInstance()
                ).also { instance = it }
            }
    }
}

/**
 * Encapsulador de exceção para instâncias de [AppError] reportadas como eventos não-fatais.
 */
class AppErrorException(message: String) : Exception(message)
