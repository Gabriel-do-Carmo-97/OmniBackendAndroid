package br.wgc.omnibackend.bundle.edge

import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniEdgeServerlessTest {

    @Test
    fun `bundle edge serverless exposes required facades`() {
        assertNotNull(OmniEdgeServerless.cloudflare)
        assertNotNull(OmniEdgeServerless.supabase)
        assertNotNull(OmniEdgeServerless.hybrid)
    }
}
