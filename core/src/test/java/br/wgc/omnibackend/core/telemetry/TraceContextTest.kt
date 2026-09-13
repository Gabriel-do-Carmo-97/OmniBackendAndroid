package br.wgc.omnibackend.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TraceContextTest {

    @Test
    fun `createTraceParent generates valid W3C format`() {
        val traceParent = TraceContext.createTraceParent()
        val parts = traceParent.split("-")

        assertEquals(4, parts.size)
        assertEquals("00", parts[0])
        assertEquals(32, parts[1].length)
        assertEquals(16, parts[2].length)
        assertEquals("01", parts[3])
    }

    @Test
    fun `extractTraceId extracts traceId correctly`() {
        val traceParent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
        val extracted = TraceContext.extractTraceId(traceParent)

        assertNotNull(extracted)
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", extracted)
    }
}
