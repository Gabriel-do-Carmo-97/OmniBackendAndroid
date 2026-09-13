package br.wgc.omnibackend.core

import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.utils.AppError
import br.wgc.omnibackend.core.utils.message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para a hierarquia selada [AppError] e modelo [OmniUser].
 */
class AppErrorTest {

    @Test
    fun `AppError Auth errors carry default and custom descriptions`() {
        val defaultError = AppError.Auth.UserNotFound
        assertTrue(defaultError.message.contains("encontrado"))

        val customError = AppError.Generic.GenericException("Custom auth failure")
        assertEquals("Custom auth failure", customError.message)
    }

    @Test
    fun `AppError Firestore errors carry valid descriptions`() {
        val notFound = AppError.Firestore.DocumentNotFound
        assertTrue(notFound.message.contains("documento"))

        val permissionDenied = AppError.Firestore.PermissionDenied
        assertTrue(permissionDenied.message.contains("negada"))
    }

    @Test
    fun `OmniUser id getter mirrors uid`() {
        val user = OmniUser(
            uid = "abc_123",
            email = "dev@omni.io",
            displayName = "Omni Dev",
            isEmailVerified = true,
            isAnonymous = false,
        )

        assertEquals("abc_123", user.id)
        assertEquals("abc_123", user.uid)
        assertEquals("dev@omni.io", user.email)
        assertEquals("Omni Dev", user.displayName)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
