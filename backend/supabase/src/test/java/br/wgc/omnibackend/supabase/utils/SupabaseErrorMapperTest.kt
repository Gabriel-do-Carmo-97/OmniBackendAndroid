package br.wgc.omnibackend.supabase.utils

import br.wgc.omnibackend.core.utils.AppError
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SupabaseErrorMapperTest {

    private fun createRestException(
        statusCode: Int,
        message: String
    ): RestException {
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        val mockRequest = mockk<HttpRequest>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.fromValue(statusCode)
        every { mockResponse.request } returns mockRequest
        every { mockRequest.url } returns Url("https://example.com")
        every { mockRequest.headers } returns Headers.Empty
        every { mockRequest.method } returns HttpMethod.Post
        return RestException(
            error = message,
            description = message,
            response = mockResponse
        )
    }

    @Test
    fun `mapAuthError returns Network on IOException`() {
        val error = SupabaseErrorMapper.mapAuthError(IOException("Connection failed"))
        assertTrue(error is AppError.Generic.Network)
    }

    @Test
    fun `mapAuthError returns EmailAlreadyInUse on 400 user already registered`() {
        val exception = createRestException(400, "User already registered")
        val error = SupabaseErrorMapper.mapAuthError(exception)
        assertTrue(error is AppError.Auth.EmailAlreadyInUse)
    }

    @Test
    fun `mapAuthError returns InvalidCredentials on 400 invalid login credentials`() {
        val exception = createRestException(400, "Invalid login credentials")
        val error = SupabaseErrorMapper.mapAuthError(exception)
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapAuthError returns UserNotFound on 404`() {
        val exception = createRestException(404, "User not found")
        val error = SupabaseErrorMapper.mapAuthError(exception)
        assertTrue(error is AppError.Auth.UserNotFound)
    }

    @Test
    fun `mapDatabaseError returns PermissionDenied on 401 or 403`() {
        val exception = createRestException(403, "Permission denied")
        val error = SupabaseErrorMapper.mapDatabaseError(exception)
        assertTrue(error is AppError.Firestore.PermissionDenied)
    }

    @Test
    fun `mapDatabaseError returns DocumentNotFound on 404`() {
        val exception = createRestException(404, "Row not found")
        val error = SupabaseErrorMapper.mapDatabaseError(exception)
        assertTrue(error is AppError.Firestore.DocumentNotFound)
    }

    @Test
    fun `mapStorageError returns QuotaExceeded on 413`() {
        val exception = createRestException(413, "Payload too large")
        val error = SupabaseErrorMapper.mapStorageError(exception)
        assertTrue(error is AppError.Storage.QuotaExceeded)
    }

    @Test
    fun `mapStorageError returns ObjectNotFound on 404`() {
        val exception = createRestException(404, "Object not found")
        val error = SupabaseErrorMapper.mapStorageError(exception)
        assertTrue(error is AppError.Storage.ObjectNotFound)
    }
}
