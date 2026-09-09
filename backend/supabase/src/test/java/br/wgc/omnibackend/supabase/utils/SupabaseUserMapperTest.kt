package br.wgc.omnibackend.supabase.utils

import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseUserMapperTest {

    @Test
    fun `toOmniUser maps fields correctly from UserInfo with full metadata`() {
        val metadata = buildJsonObject {
            put("full_name", "Gabriel do Carmo")
            put("avatar_url", "https://example.com/avatar.png")
            put("is_anonymous", false)
        }

        val userInfo = UserInfo(
            id = "supabase_user_123",
            email = "gabriel@example.com",
            userMetadata = metadata,
            appMetadata = buildJsonObject {},
            aud = "authenticated",
            createdAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            lastSignInAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            emailConfirmedAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            phone = null,
            role = "authenticated",
            confirmationSentAt = null,
            confirmedAt = null,
            recoverySentAt = null,
            emailChangeSentAt = null,
            newEmail = null,
            invitedAt = null,
            actionLink = null,
            phoneConfirmedAt = null,
            phoneChangeSentAt = null,
            newPhone = null,
            factors = emptyList(),
            identities = emptyList()
        )

        val omniUser = SupabaseUserMapper.toOmniUser(userInfo)

        assertEquals("supabase_user_123", omniUser.uid)
        assertEquals("gabriel@example.com", omniUser.email)
        assertEquals("Gabriel do Carmo", omniUser.displayName)
        assertEquals("https://example.com/avatar.png", omniUser.photoUrl)
        assertTrue(omniUser.isEmailVerified)
        assertFalse(omniUser.isAnonymous)
    }

    @Test
    fun `toOmniUser handles null optional fields gracefully`() {
        val userInfo = UserInfo(
            id = "guest_456",
            email = null,
            userMetadata = null,
            appMetadata = buildJsonObject {},
            aud = "authenticated",
            createdAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = kotlinx.datetime.Instant.parse("2026-01-01T00:00:00Z"),
            lastSignInAt = null,
            emailConfirmedAt = null,
            phone = null,
            role = "authenticated",
            confirmationSentAt = null,
            confirmedAt = null,
            recoverySentAt = null,
            emailChangeSentAt = null,
            newEmail = null,
            invitedAt = null,
            actionLink = null,
            phoneConfirmedAt = null,
            phoneChangeSentAt = null,
            newPhone = null,
            factors = emptyList(),
            identities = null
        )

        val omniUser = SupabaseUserMapper.toOmniUser(userInfo)

        assertEquals("guest_456", omniUser.uid)
        assertNull(omniUser.email)
        assertNull(omniUser.displayName)
        assertNull(omniUser.photoUrl)
        assertFalse(omniUser.isEmailVerified)
        assertTrue(omniUser.isAnonymous)
    }
}
