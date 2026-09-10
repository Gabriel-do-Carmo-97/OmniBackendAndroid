package br.wgc.omnibackend.bundle.classic

import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniBaaSClassicTest {

    @Test
    fun `bundle baas classic exposes required facades`() {
        assertNotNull(OmniBaaSClassic.back4App)
        assertNotNull(OmniBaaSClassic.firebase)
        assertNotNull(OmniBaaSClassic.hybrid)
    }
}
