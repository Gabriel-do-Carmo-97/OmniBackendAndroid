package br.wgc.omnibackend.pocketbase.utils

import br.wgc.omnibackend.core.utils.AppError
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class PocketBaseErrorMapperTest {

    @Test
    fun `mapStatusCode returns InvalidCredentials on 401`() {
        val error = PocketBaseErrorMapper.mapStatusCode(401, "auth")
        assertTrue(error is AppError.Auth.InvalidCredentials)
    }

    @Test
    fun `mapStatusCode returns UserNotFound on 404 for auth`() {
        val error = PocketBaseErrorMapper.mapStatusCode(404, "auth")
        assertTrue(error is AppError.Auth.UserNotFound)
    }

    @Test
    fun `mapStatusCode returns DocumentNotFound on 404 for firestore`() {
        val error = PocketBaseErrorMapper.mapStatusCode(404, "firestore")
        assertTrue(error is AppError.Firestore.DocumentNotFound)
    }

    @Test
    fun `mapThrowable returns Network on UnknownHostException`() {
        val error = PocketBaseErrorMapper.mapThrowable(UnknownHostException("Network error"))
        assertTrue(error is AppError.Generic.Network)
    }
}
