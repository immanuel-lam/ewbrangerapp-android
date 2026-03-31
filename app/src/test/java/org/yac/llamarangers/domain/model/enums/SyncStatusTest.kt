package org.yac.llamarangers.domain.model.enums

import org.junit.Assert.*
import org.junit.Test

class SyncStatusTest {

    @Test
    fun `fromValue returns correct status for each known value`() {
        assertEquals(SyncStatus.PENDING_CREATE, SyncStatus.fromValue(0))
        assertEquals(SyncStatus.PENDING_UPDATE, SyncStatus.fromValue(1))
        assertEquals(SyncStatus.PENDING_DELETE, SyncStatus.fromValue(2))
        assertEquals(SyncStatus.SYNCED, SyncStatus.fromValue(3))
    }

    @Test
    fun `fromValue defaults to PENDING_CREATE for unknown value`() {
        assertEquals(SyncStatus.PENDING_CREATE, SyncStatus.fromValue(-1))
        assertEquals(SyncStatus.PENDING_CREATE, SyncStatus.fromValue(99))
    }

    @Test
    fun `every status has a non-empty iconName`() {
        SyncStatus.entries.forEach { status ->
            assertTrue(status.iconName.isNotBlank())
        }
    }

    @Test
    fun `value roundtrips through fromValue`() {
        SyncStatus.entries.forEach { status ->
            assertEquals(status, SyncStatus.fromValue(status.value))
        }
    }
}
