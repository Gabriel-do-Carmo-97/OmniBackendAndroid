package br.wgc.omnibackend.amplify.utils

import br.wgc.omnibackend.core.utils.AppError
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
}
