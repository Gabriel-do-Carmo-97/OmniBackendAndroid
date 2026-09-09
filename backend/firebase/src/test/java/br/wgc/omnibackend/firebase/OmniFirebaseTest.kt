package br.wgc.omnibackend.firebase

import br.wgc.omnibackend.firebase.security.AppCheckManager
import org.junit.Assert.assertNotNull
import org.junit.Test

class OmniFirebaseTest {

    @Test
    fun `appCheck property returns AppCheckManager instance`() {
        val appCheck = OmniFirebase.appCheck
        assertNotNull(appCheck)
        assert(appCheck === AppCheckManager)
    }
}

