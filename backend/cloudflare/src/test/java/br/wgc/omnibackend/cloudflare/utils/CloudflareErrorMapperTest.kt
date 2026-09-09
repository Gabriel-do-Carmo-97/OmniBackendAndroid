package br.wgc.omnibackend.cloudflare.utils

import br.wgc.omnibackend.core.utils.AppError
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class CloudflareErrorMapperTest {

    @Test
    fun `mapStatusCode returns InvalidCredentials on 401`() {
        val error = CloudflareErrorMapper.mapStatusCode(401, "auth")
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapStatusCode returns ObjectNotFound on 404 for storage`() {
        val error = CloudflareErrorMapper.mapStatusCode(404, "storage")
        assertTrue(error is AppError.Storage.ObjectNotFound)
    }

    @Test
    fun `mapThrowable returns Network on UnknownHostException`() {
        val error = CloudflareErrorMapper.mapThrowable(UnknownHostException("Host unresolvable"))
        assertTrue(error is AppError.Generic.Network)
    }
}
