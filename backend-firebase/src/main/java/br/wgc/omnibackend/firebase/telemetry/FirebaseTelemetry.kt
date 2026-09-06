package br.wgc.omnibackend.firebase.telemetry

import br.wgc.omnibackend.firebase.utils.AppError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level observability and telemetry manager for Firebase.
 *
 * Standardizes:
 * - Reporting domain [AppError] failures and non-fatal exceptions to Crashlytics.
 * - Profiling critical synchronous and asynchronous execution blocks using Firebase Performance traces.
 */
@Singleton
class FirebaseTelemetry @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
    @PublishedApi internal val performance: FirebasePerformance
) {

    /**
     * Reports an [AppError] to Firebase Crashlytics as a non-fatal error.
     * Automatically extracts underlying causes for [AppError.Generic.Unknown] and [AppError.Auth.Generic].
     *
     * @param error The domain error to record.
     * @param attributes Optional contextual attributes attached as custom keys.
     */
    fun recordError(error: AppError, attributes: Map<String, Any> = emptyMap()) {
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
     * Records an unhandled exception or non-fatal throwable to Crashlytics.
     */
    fun recordException(throwable: Throwable, attributes: Map<String, Any> = emptyMap()) {
        attributes.forEach { (key, value) -> setAttribute(key, value) }
        crashlytics.recordException(throwable)
    }

    /**
     * Logs a diagnostic message into the Crashlytics session buffer.
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Associates a user identifier with Crashlytics reports.
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    /**
     * Sets a custom attribute key-value pair in Crashlytics.
     */
    fun setAttribute(key: String, value: Any) {
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
     * Measures the execution time of a synchronous block of code using Firebase Performance.
     *
     * @param traceName Name of the trace metric.
     * @param block The block to execute and measure.
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
     * Measures the execution time of a suspending coroutine block of code using Firebase Performance.
     *
     * @param traceName Name of the trace metric.
     * @param block The suspending block to execute and measure.
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
         * Obtains the default singleton instance of [FirebaseTelemetry].
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
 * Exception wrapper for domain [AppError] instances reported as non-fatals.
 */
class AppErrorException(message: String) : Exception(message)

