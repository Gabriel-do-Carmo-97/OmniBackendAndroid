package br.wgc.omnibackend.appwrite.utils

import br.wgc.omnibackend.core.utils.AppError
import io.appwrite.exceptions.AppwriteException
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class AppwriteErrorMapperTest {

    @Test
    fun `mapThrowable returns Network on UnknownHostException`() {
        val error = AppwriteErrorMapper.mapThrowable(UnknownHostException("Unable to resolve host"))
        assertTrue(error is AppError.Generic.Network)
    }

    @Test
    fun `mapException returns InvalidCredentials on 401 code`() {
        val exception = AppwriteException("Unauthorized", 401, "user_unauthorized")
        val error = AppwriteErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapException returns EmailAlreadyInUse on 409 code`() {
        val exception = AppwriteException("User already exists", 409, "user_already_exists")
        val error = AppwriteErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Auth.EmailAlreadyInUse)
    }

    @Test
    fun `mapException returns UserNotFound on 404 with auth context`() {
        val exception = AppwriteException("User not found", 404, "user_not_found")
        val error = AppwriteErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Auth.UserNotFound)
    }

    @Test
    fun `mapException returns PermissionDenied on 403 with firestore context`() {
        val exception = AppwriteException("Unauthorized access", 403, "general_unauthorized_scope")
        val error = AppwriteErrorMapper.mapException(exception, "firestore")
        assertTrue(error is AppError.Firestore.PermissionDenied)
    }

    @Test
    fun `mapException returns DocumentNotFound on 404 with firestore context`() {
        val exception = AppwriteException("Document not found", 404, "document_not_found")
        val error = AppwriteErrorMapper.mapException(exception, "firestore")
        assertTrue(error is AppError.Firestore.DocumentNotFound)
    }

    @Test
    fun `mapException returns ObjectNotFound on 404 with storage context`() {
        val exception = AppwriteException("File not found", 404, "storage_file_not_found")
        val error = AppwriteErrorMapper.mapException(exception, "storage")
        assertTrue(error is AppError.Storage.ObjectNotFound)
    }

    @Test
    fun `mapException returns QuotaExceeded on 507 code`() {
        val exception = AppwriteException("Storage quota exceeded", 507, "storage_quota_exceeded")
        val error = AppwriteErrorMapper.mapException(exception, "storage")
        assertTrue(error is AppError.Storage.QuotaExceeded)
    }
}
