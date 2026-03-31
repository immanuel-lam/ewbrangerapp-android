package org.yac.llamarangers.domain.model.enums

import org.junit.Assert.*
import org.junit.Test

class TreatmentMethodTest {

    @Test
    fun `fromValue returns correct method for each known value`() {
        assertEquals(TreatmentMethod.CUT_STUMP, TreatmentMethod.fromValue("cutStump"))
        assertEquals(TreatmentMethod.SPLAT_GUN, TreatmentMethod.fromValue("splatGun"))
        assertEquals(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.fromValue("foliarSpray"))
        assertEquals(TreatmentMethod.BASAL_BARK, TreatmentMethod.fromValue("basalBark"))
    }

    @Test
    fun `fromValue defaults to FOLIAR_SPRAY for unrecognised value`() {
        assertEquals(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.fromValue(""))
        assertEquals(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.fromValue("fire"))
    }

    @Test
    fun `every method has non-empty instructions and displayName`() {
        TreatmentMethod.entries.forEach { method ->
            assertTrue(method.displayName.isNotBlank())
            assertTrue(method.instructions.isNotBlank())
            assertTrue(method.iconName.isNotBlank())
        }
    }

    @Test
    fun `value roundtrips through fromValue`() {
        TreatmentMethod.entries.forEach { method ->
            assertEquals(method, TreatmentMethod.fromValue(method.value))
        }
    }
}
