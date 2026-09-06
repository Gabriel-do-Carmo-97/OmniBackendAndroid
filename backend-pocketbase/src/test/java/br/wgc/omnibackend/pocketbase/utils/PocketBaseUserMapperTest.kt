package br.wgc.omnibackend.pocketbase.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PocketBaseUserMapperTest {

    @Test
    fun `toOmniUser maps record fields correctly`() {
        val record = mapOf<String, Any?>(
            "id" to "pb_user_123",
            "email" to "gabriel@example.com",
            "name" to "Gabriel do Carmo",
            "avatar" to "avatar_1.png",
            "collectionId" to "users_col_456",
            "verified" to true
        )

        val user = PocketBaseUserMapper.toOmniUser(record, "https://pb.example.com")

        assertEquals("pb_user_123", user.uid)
        assertEquals("gabriel@example.com", user.email)
        assertEquals("Gabriel do Carmo", user.displayName)
        assertEquals("https://pb.example.com/api/files/users_col_456/pb_user_123/avatar_1.png", user.photoUrl)
        assertTrue(user.isEmailVerified)
        assertFalse(user.isAnonymous)
    }
}
