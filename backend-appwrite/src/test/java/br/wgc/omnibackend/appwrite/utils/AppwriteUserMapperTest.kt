package br.wgc.omnibackend.appwrite.utils

import io.appwrite.models.User
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppwriteUserMapperTest {

    @Test
    fun `toOmniUser maps fields correctly from Appwrite User model`() {
        val appwriteUser = mockk<User<Map<String, Any>>>(relaxed = true)
        every { appwriteUser.id } returns "appwrite_123"
        every { appwriteUser.email } returns "gabriel@example.com"
        every { appwriteUser.name } returns "Gabriel do Carmo"
        every { appwriteUser.emailVerification } returns true

        val omniUser = AppwriteUserMapper.toOmniUser(appwriteUser)

        assertEquals("appwrite_123", omniUser.uid)
        assertEquals("gabriel@example.com", omniUser.email)
        assertEquals("Gabriel do Carmo", omniUser.displayName)
        assertNull(omniUser.photoUrl)
        assertTrue(omniUser.isEmailVerified)
        assertFalse(omniUser.isAnonymous)
    }

    @Test
    fun `toOmniUser handles anonymous session (blank email and name)`() {
        val anonymousUser = mockk<User<Map<String, Any>>>(relaxed = true)
        every { anonymousUser.id } returns "anon_789"
        every { anonymousUser.email } returns ""
        every { anonymousUser.name } returns ""
        every { anonymousUser.emailVerification } returns false

        val omniUser = AppwriteUserMapper.toOmniUser(anonymousUser)

        assertEquals("anon_789", omniUser.uid)
        assertEquals("", omniUser.email)
        assertEquals("", omniUser.displayName)
        assertNull(omniUser.photoUrl)
        assertFalse(omniUser.isEmailVerified)
        assertTrue(omniUser.isAnonymous)
    }
}
