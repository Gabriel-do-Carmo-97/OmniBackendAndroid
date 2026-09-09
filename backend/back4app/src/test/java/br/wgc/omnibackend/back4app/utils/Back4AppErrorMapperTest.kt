package br.wgc.omnibackend.back4app.utils

import br.wgc.omnibackend.core.utils.AppError
import com.parse.ParseException
import org.junit.Assert.assertTrue
import org.junit.Test

class Back4AppErrorMapperTest {

    @Test
    fun `mapException maps OBJECT_NOT_FOUND to UserNotFound for auth`() {
        val exception = ParseException(ParseException.OBJECT_NOT_FOUND, "User not found")
        val error = Back4AppErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Auth.UserNotFound)
    }

    @Test
    fun `mapException maps USERNAME_TAKEN to EmailAlreadyInUse`() {
        val exception = ParseException(ParseException.USERNAME_TAKEN, "Username taken")
        val error = Back4AppErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Auth.EmailAlreadyInUse)
    }

    @Test
    fun `mapException maps CONNECTION_FAILED to Network`() {
        val exception = ParseException(ParseException.CONNECTION_FAILED, "Connection failed")
        val error = Back4AppErrorMapper.mapException(exception, "auth")
        assertTrue(error is AppError.Generic.Network)
    }
}
