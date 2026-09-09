package br.wgc.omnibackend.amplify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmplifyUserMapperTest {

    @Test
    fun `toOmniUser maps attributes correctly`() {
        val attributes = mapOf<String, Any?>(
            "sub" to "amplify_sub_123",
            "email" to "gabriel@example.com",
            "name" to "Gabriel do Carmo",
            "picture" to "https://s3.amazonaws.com/avatar.png",
            "email_verified" to true
        )

        val user = AmplifyUserMapper.toOmniUser(attributes)

        assertEquals("amplify_sub_123", user.uid)
        assertEquals("gabriel@example.com", user.email)
        assertEquals("Gabriel do Carmo", user.displayName)
        assertEquals("https://s3.amazonaws.com/avatar.png", user.photoUrl)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
