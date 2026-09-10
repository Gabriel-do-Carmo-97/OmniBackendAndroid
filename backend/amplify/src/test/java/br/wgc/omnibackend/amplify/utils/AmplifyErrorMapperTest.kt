package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.utils.AppError
import com.amplifyframework.auth.AuthException
import com.amplifyframework.storage.StorageException
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class AmplifyErrorMapperTest {

    @Test
    fun `mapStatusCode returns InvalidCredentials on 401`() {
        val error = AmplifyErrorMapper.mapStatusCode(401, "auth")
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapThrowable returns Network on UnknownHostException`() {
        val error = AmplifyErrorMapper.mapThrowable(UnknownHostException("S3 unreachable"))
        assertTrue(error is AppError.Generic.Network)
    }

    @Test
    fun `mapThrowable returns UserNotFound on AuthException with user not found`() {
        val ex = AuthException("User does not exist", "Please check username")
        val error = AmplifyErrorMapper.mapThrowable(ex, "auth")
        assertTrue(error is AppError.Auth.UserNotFound)
    }

    @Test
    fun `mapThrowable returns ObjectNotFound on StorageException with NoSuchKey`() {
        val ex = StorageException("NoSuchKey: file not found", "Check file path")
        val error = AmplifyErrorMapper.mapThrowable(ex, "storage")
        assertTrue(error is AppError.Storage.ObjectNotFound)
    }
}
