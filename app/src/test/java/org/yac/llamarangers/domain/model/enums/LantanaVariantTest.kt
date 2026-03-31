package org.yac.llamarangers.domain.model.enums

import org.junit.Assert.*
import org.junit.Test

class LantanaVariantTest {

    @Test
    fun `fromValue returns correct variant for each known value`() {
        assertEquals(LantanaVariant.PINK, LantanaVariant.fromValue("pink"))
        assertEquals(LantanaVariant.RED, LantanaVariant.fromValue("red"))
        assertEquals(LantanaVariant.PINK_EDGED_RED, LantanaVariant.fromValue("pinkEdgedRed"))
        assertEquals(LantanaVariant.ORANGE, LantanaVariant.fromValue("orange"))
        assertEquals(LantanaVariant.WHITE, LantanaVariant.fromValue("white"))
        assertEquals(LantanaVariant.UNKNOWN, LantanaVariant.fromValue("unknown"))
    }

    @Test
    fun `fromValue returns UNKNOWN for unrecognised value`() {
        assertEquals(LantanaVariant.UNKNOWN, LantanaVariant.fromValue("purple"))
        assertEquals(LantanaVariant.UNKNOWN, LantanaVariant.fromValue(""))
        assertEquals(LantanaVariant.UNKNOWN, LantanaVariant.fromValue("PINK"))  // case sensitive
    }

    @Test
    fun `every variant has a non-empty displayName`() {
        LantanaVariant.entries.forEach { variant ->
            assertTrue("${variant.name} has empty displayName", variant.displayName.isNotBlank())
        }
    }

    @Test
    fun `every variant has at least one control method`() {
        LantanaVariant.entries.forEach { variant ->
            assertTrue(
                "${variant.name} has no control methods",
                variant.controlMethods.isNotEmpty()
            )
        }
    }

    @Test
    fun `every variant has non-empty distinguishing features`() {
        LantanaVariant.entries.forEach { variant ->
            assertTrue(variant.distinguishingFeatures.isNotBlank())
        }
    }

    @Test
    fun `only PINK has biocontrol concern`() {
        assertTrue(LantanaVariant.PINK.hasBiocontrolConcern)
        LantanaVariant.entries.filter { it != LantanaVariant.PINK }.forEach {
            assertFalse("${it.name} should not have biocontrol concern", it.hasBiocontrolConcern)
        }
    }

    @Test
    fun `value roundtrips through fromValue`() {
        LantanaVariant.entries.forEach { variant ->
            assertEquals(variant, LantanaVariant.fromValue(variant.value))
        }
    }
}
