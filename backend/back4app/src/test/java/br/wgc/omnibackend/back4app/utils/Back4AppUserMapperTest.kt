package br.wgc.omnibackend.back4app.utils

import com.parse.ParseFile
import com.parse.ParseUser
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Back4AppUserMapperTest {

    @Test
    fun `toOmniUser maps fields correctly from ParseUser`() {
        val mockParseFile = mockk<ParseFile>()
        every { mockParseFile.url } returns "https://example.com/avatar.png"

        val mockUser = mockk<ParseUser>()
        every { mockUser.objectId } returns "parse_123"
        every { mockUser.email } returns "gabriel@example.com"
        every { mockUser.username } returns "gabriel"
        every { mockUser.getString("name") } returns "Gabriel do Carmo"
        every { mockUser.getParseFile("avatar") } returns mockParseFile
        every { mockUser.getBoolean("emailVerified") } returns true

        val omniUser = Back4AppUserMapper.toOmniUser(mockUser)

        assertEquals("parse_123", omniUser.uid)
        assertEquals("gabriel@example.com", omniUser.email)
        assertEquals("Gabriel do Carmo", omniUser.displayName)
        assertEquals("https://example.com/avatar.png", omniUser.photoUrl)
        assertTrue(omniUser.isEmailVerified)
        assertFalse(omniUser.isAnonymous)
    }
}
