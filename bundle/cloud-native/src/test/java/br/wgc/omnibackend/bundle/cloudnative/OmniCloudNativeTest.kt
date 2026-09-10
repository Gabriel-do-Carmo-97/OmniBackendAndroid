package br.wgc.omnibackend.bundle.cloudnative

import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniCloudNativeTest {

    @Test
    fun `bundle cloud native exposes required facades`() {
        assertNotNull(OmniCloudNative.firebase)
        assertNotNull(OmniCloudNative.supabase)
        assertNotNull(OmniCloudNative.amplify)
        assertNotNull(OmniCloudNative.hybrid)
    }
}
