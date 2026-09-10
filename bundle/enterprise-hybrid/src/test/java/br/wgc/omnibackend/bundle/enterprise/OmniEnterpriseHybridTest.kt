package br.wgc.omnibackend.bundle.enterprise

import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniEnterpriseHybridTest {

    @Test
    fun `bundle enterprise hybrid exposes required facades`() {
        assertNotNull(OmniEnterpriseHybrid.rest)
        assertNotNull(OmniEnterpriseHybrid.firebase)
        assertNotNull(OmniEnterpriseHybrid.hybrid)
    }
}
