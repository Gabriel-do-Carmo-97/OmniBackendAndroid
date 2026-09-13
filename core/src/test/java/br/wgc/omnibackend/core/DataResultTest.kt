package br.wgc.omnibackend.core

import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.core.utils.fold
import br.wgc.omnibackend.core.utils.map
import br.wgc.omnibackend.core.utils.onFailure
import br.wgc.omnibackend.core.utils.onSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para a classe monádica [DataResult] e suas extensões funcionais.
 */
class DataResultTest {

    @Test
    fun `Success holds correct data and onSuccess executes`() {
        val result: DataResult<String> = DataResult.Success("test_data")
        var executed = false

        result.onSuccess { data ->
            assertEquals("test_data", data)
            executed = true
        }

        assertTrue(executed)
        assertEquals("test_data", result.getOrNull())
    }

    @Test
    fun `Failure holds correct error and onFailure executes`() {
        val error = AppError.Auth.UserNotFound
        val result: DataResult<String> = DataResult.Failure(error)
        var capturedError: AppError? = null

        result.onFailure { capturedError = it }

        assertEquals(error, capturedError)
        assertNull(result.getOrNull())
    }

    @Test
    fun `map transforms Success value and preserves Failure`() {
        val successResult: DataResult<Int> = DataResult.Success(10)
        val mappedSuccess = successResult.map { it * 2 }

        assertTrue(mappedSuccess is DataResult.Success)
        assertEquals(20, mappedSuccess.getOrNull())

        val failureResult: DataResult<Int> = DataResult.Failure(AppError.Generic.Network)
        val mappedFailure = failureResult.map { it * 2 }

        assertTrue(mappedFailure is DataResult.Failure)
        assertEquals(AppError.Generic.Network, (mappedFailure as DataResult.Failure).error)
    }

    @Test
    fun `fold reduces both Success and Failure correctly`() {
        val success: DataResult<String> = DataResult.Success("Omni")
        val successFold = success.fold(
            onSuccess = { "Hello $it" },
            onFailure = { "Error" },
        )
        assertEquals("Hello Omni", successFold)

        val failure: DataResult<String> = DataResult.Failure(AppError.Auth.InvalidCredentials)
        val failureFold = failure.fold(
            onSuccess = { "Success" },
            onFailure = { "Failed: ${it::class.java.simpleName}" },
        )
        assertEquals("Failed: InvalidCredentials", failureFold)
    }
}
