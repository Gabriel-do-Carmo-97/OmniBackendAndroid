package br.wgc.omnibackend.rest.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestUserMapperTest {

    @Test
    fun `toOmniUser maps json user object correctly`() {
        val json = mapOf<String, Any?>(
            "id" to "rest_user_123",
            "email" to "gabriel@example.com",
            "name" to "Gabriel do Carmo",
            "photoUrl" to "https://api.empresa.com/avatars/user_123.jpg",
            "isEmailVerified" to true,
            "isAnonymous" to false
        )

        val user = RestUserMapper.toOmniUser(json)

        assertEquals("rest_user_123", user.uid)
        assertEquals("gabriel@example.com", user.email)
        assertEquals("Gabriel do Carmo", user.displayName)
        assertEquals("https://api.empresa.com/avatars/user_123.jpg", user.photoUrl)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
