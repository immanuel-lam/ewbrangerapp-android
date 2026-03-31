package org.yac.llamarangers.domain.model.enums

import org.junit.Assert.*
import org.junit.Test

class InfestationSizeTest {

    @Test
    fun `fromValue returns correct size for each known value`() {
        assertEquals(InfestationSize.SMALL, InfestationSize.fromValue("small"))
        assertEquals(InfestationSize.MEDIUM, InfestationSize.fromValue("medium"))
        assertEquals(InfestationSize.LARGE, InfestationSize.fromValue("large"))
    }

    @Test
    fun `fromValue defaults to SMALL for unrecognised value`() {
        assertEquals(InfestationSize.SMALL, InfestationSize.fromValue(""))
        assertEquals(InfestationSize.SMALL, InfestationSize.fromValue("huge"))
        assertEquals(InfestationSize.SMALL, InfestationSize.fromValue("LARGE"))
    }

    @Test
    fun `every size has a non-empty displayName and areaDescription`() {
        InfestationSize.entries.forEach { size ->
            assertTrue(size.displayName.isNotBlank())
            assertTrue(size.areaDescription.isNotBlank())
        }
    }

    @Test
    fun `value roundtrips through fromValue`() {
        InfestationSize.entries.forEach { size ->
            assertEquals(size, InfestationSize.fromValue(size.value))
        }
    }
}
