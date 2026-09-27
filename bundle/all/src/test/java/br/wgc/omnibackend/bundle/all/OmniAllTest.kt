package br.wgc.omnibackend.bundle.all

import br.wgc.omnibackend.bundle.all.di.AllBackendsModule
import br.wgc.omnibackend.bundle.hybrid.OmniHybrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniAllTest {

    @Test
    fun `OmniAll should expose hybrid orchestrator`() {
        assertNotNull(OmniAll.hybrid)
        assertEquals(OmniHybrid, OmniAll.hybrid)
    }

    @Test
    fun `AllBackendsModule should provide OmniAll singleton instance`() {
        val omniAll = AllBackendsModule.provideOmniAll()
        assertNotNull(omniAll)
        assertEquals(OmniAll, omniAll)
    }

    @Test
    fun `AllBackendsModule qualifier master constant should have correct value`() {
        assertEquals("all_master", AllBackendsModule.QUALIFIER_MASTER)
    }
}
