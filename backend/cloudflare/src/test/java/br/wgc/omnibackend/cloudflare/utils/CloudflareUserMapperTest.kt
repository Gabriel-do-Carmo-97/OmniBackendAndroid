package br.wgc.omnibackend.cloudflare.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudflareUserMapperTest {

    @Test
    fun `toOmniUser maps worker response map correctly`() {
        val response = mapOf<String, Any?>(
            "uid" to "cf_user_123",
            "email" to "gabriel@example.com",
            "displayName" to "Gabriel do Carmo",
            "photoUrl" to "https://r2.example.com/avatar.png",
            "isEmailVerified" to true,
            "isAnonymous" to false,
        )

        val user = CloudflareUserMapper.toOmniUser(response)

        assertEquals("cf_user_123", user.uid)
        assertEquals("gabriel@example.com", user.email)
        assertEquals("Gabriel do Carmo", user.displayName)
        assertEquals("https://r2.example.com/avatar.png", user.photoUrl)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
