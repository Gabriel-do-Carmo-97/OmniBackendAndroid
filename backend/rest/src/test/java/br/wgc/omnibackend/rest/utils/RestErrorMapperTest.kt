package br.wgc.omnibackend.rest.utils

import br.wgc.omnibackend.core.utils.AppError
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class RestErrorMapperTest {

    @Test
    fun `mapStatusCode returns InvalidCredentials on 401`() {
        val error = RestErrorMapper.mapStatusCode(401, "auth")
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapThrowable returns Network on UnknownHostException`() {
        val error = RestErrorMapper.mapThrowable(UnknownHostException("API unreachable"))
        assertTrue(error is AppError.Generic.Network)
    }
}
