package br.wgc.omnibackend.bundle.selfhosted

import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniSelfHostedTest {

    @Test
    fun `bundle self hosted exposes required facades`() {
        assertNotNull(OmniSelfHosted.pocketBase)
        assertNotNull(OmniSelfHosted.appwrite)
        assertNotNull(OmniSelfHosted.hybrid)
    }
}
