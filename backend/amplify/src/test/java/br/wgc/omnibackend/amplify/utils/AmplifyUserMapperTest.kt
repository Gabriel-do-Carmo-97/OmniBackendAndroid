package br.wgc.omnibackend.amplify.utils

import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
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

    @Test
    fun `toOmniUser maps AuthUser and AuthUserAttributes correctly`() {
        val authUser = AuthUser("cognito_123", "user@aws.com")
        val attrs = listOf(
            AuthUserAttribute(AuthUserAttributeKey.email(), "user@aws.com"),
            AuthUserAttribute(AuthUserAttributeKey.name(), "AWS User"),
            AuthUserAttribute(AuthUserAttributeKey.emailVerified(), "true")
        )

        val user = AmplifyUserMapper.toOmniUser(authUser, attrs)

        assertEquals("cognito_123", user.uid)
        assertEquals("user@aws.com", user.email)
        assertEquals("AWS User", user.displayName)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
