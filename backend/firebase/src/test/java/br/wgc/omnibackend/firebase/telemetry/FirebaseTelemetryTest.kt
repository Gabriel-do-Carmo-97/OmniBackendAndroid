package br.wgc.omnibackend.firebase.telemetry

import br.wgc.omnibackend.core.utils.AppError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FirebaseTelemetryTest {

    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val performance: FirebasePerformance = mockk(relaxed = true)
    private val trace: Trace = mockk(relaxed = true)
    private lateinit var telemetry: FirebaseTelemetry

    @Before
    fun setUp() {
        every { performance.newTrace(any()) } returns trace
        telemetry = FirebaseTelemetry(crashlytics, performance)
    }

    @Test
    fun `recordError with Unknown unrolls and records original throwable`() {
        val originalException = RuntimeException("Boom")
        val error = AppError.Generic.Unknown(originalException)

        telemetry.recordError(error)

        verify(exactly = 1) { crashlytics.recordException(originalException) }
    }

    @Test
    fun `recordError with Auth Generic unrolls and records original exception`() {
        val originalException = Exception("Auth fail")
        val error = AppError.Auth.Generic(originalException)

        telemetry.recordError(error)

        verify(exactly = 1) { crashlytics.recordException(originalException) }
    }

    @Test
    fun `recordError with domain error logs and records AppErrorException`() {
        val error = AppError.Auth.UserNotFound

        telemetry.recordError(error)

        verify(exactly = 1) { crashlytics.log(match { it.contains("UserNotFound") }) }
        verify(exactly = 1) { crashlytics.recordException(any<AppErrorException>()) }
    }

    @Test
    fun `trace starts and stops trace`() {
        val result = telemetry.trace("test_trace") {
            "hello"
        }

        assertEquals("hello", result)
        verify(exactly = 1) { trace.start() }
        verify(exactly = 1) { trace.stop() }
    }

    @Test
    fun `traceAsync starts and stops trace`() = runTest {
        val result = telemetry.traceAsync("async_trace") {
            "async_result"
        }

        assertEquals("async_result", result)
        verify(exactly = 1) { trace.start() }
        verify(exactly = 1) { trace.stop() }
    }
}
