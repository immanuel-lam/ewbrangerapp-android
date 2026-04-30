package org.yac.llamarangers.data.local.db

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    // ── String list ──────────────────────────────────────────────────────────

    @Test
    fun `fromStringList serialises empty list`() {
        assertEquals("[]", converters.fromStringList(emptyList()))
    }

    @Test
    fun `fromStringList serialises null as empty list`() {
        assertEquals("[]", converters.fromStringList(null))
    }

    @Test
    fun `fromStringList serialises populated list`() {
        val json = converters.fromStringList(listOf("photo_1.jpg", "photo_2.jpg"))
        assertTrue(json.contains("photo_1.jpg"))
        assertTrue(json.contains("photo_2.jpg"))
    }

    @Test
    fun `toStringList deserialises empty JSON array`() {
        assertEquals(emptyList<String>(), converters.toStringList("[]"))
    }

    @Test
    fun `toStringList returns empty for null input`() {
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }

    @Test
    fun `toStringList returns empty for empty string`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
    }

    @Test
    fun `toStringList returns empty for corrupted JSON`() {
        assertEquals(emptyList<String>(), converters.toStringList("{not valid json"))
    }

    @Test
    fun `string list roundtrips`() {
        val original = listOf("a.jpg", "b.jpg", "c.jpg")
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }

    @Test
    fun `string list with special characters roundtrips`() {
        val original = listOf("photo with spaces.jpg", "path/to/file.png", "unicode_日本語.jpg")
        val json = converters.fromStringList(original)
        val restored = converters.toStringList(json)
        assertEquals(original, restored)
    }

    // ── Coordinate list ──────────────────────────────────────────────────────

    @Test
    fun `fromCoordinateList serialises empty list`() {
        assertEquals("[]", converters.fromCoordinateList(emptyList()))
    }

    @Test
    fun `fromCoordinateList serialises null as empty list`() {
        assertEquals("[]", converters.fromCoordinateList(null))
    }

    @Test
    fun `toCoordinateList returns empty for null input`() {
        assertEquals(emptyList<List<Double>>(), converters.toCoordinateList(null))
    }

    @Test
    fun `toCoordinateList returns empty for empty string`() {
        assertEquals(emptyList<List<Double>>(), converters.toCoordinateList(""))
    }

    @Test
    fun `toCoordinateList returns empty for corrupted JSON`() {
        assertEquals(emptyList<List<Double>>(), converters.toCoordinateList("not json"))
    }

    @Test
    fun `coordinate list roundtrips`() {
        val original = listOf(
            listOf(-14.7019, 143.7075),
            listOf(-14.7020, 143.7080),
            listOf(-14.7015, 143.7078)
        )
        val json = converters.fromCoordinateList(original)
        val restored = converters.toCoordinateList(json)
        assertEquals(original.size, restored.size)
        original.zip(restored).forEach { (expected, actual) ->
            assertEquals(expected[0], actual[0], 0.0001)
            assertEquals(expected[1], actual[1], 0.0001)
        }
    }

    @Test
    fun `coordinate list handles single point`() {
        val original = listOf(listOf(0.0, 0.0))
        val json = converters.fromCoordinateList(original)
        val restored = converters.toCoordinateList(json)
        assertEquals(1, restored.size)
        assertEquals(0.0, restored[0][0], 0.0001)
        assertEquals(0.0, restored[0][1], 0.0001)
    }

    @Test
    fun `coordinate list handles negative coordinates`() {
        val original = listOf(listOf(-33.8688, 151.2093))  // Sydney
        val json = converters.fromCoordinateList(original)
        val restored = converters.toCoordinateList(json)
        assertEquals(-33.8688, restored[0][0], 0.0001)
        assertEquals(151.2093, restored[0][1], 0.0001)
    }
}
