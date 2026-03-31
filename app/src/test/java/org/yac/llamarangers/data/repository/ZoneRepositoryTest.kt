package org.yac.llamarangers.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.yac.llamarangers.data.local.dao.InfestationZoneDao
import org.yac.llamarangers.data.local.dao.InfestationZoneSnapshotDao
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.db.AppDatabase
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.SyncStatus

class ZoneRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var zoneDao: InfestationZoneDao
    private lateinit var snapshotDao: InfestationZoneSnapshotDao
    private lateinit var sightingDao: SightingLogDao
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var repository: ZoneRepository

    @Before
    fun setUp() {
        db = mock()
        zoneDao = mock()
        snapshotDao = mock()
        sightingDao = mock()
        syncQueueDao = mock()
        repository = ZoneRepository(db, zoneDao, snapshotDao, sightingDao, syncQueueDao)
    }

    @Test
    fun `createZone inserts entity with correct variant value`() = runTest {
        repository.createZone(name = "Test Zone", dominantVariant = LantanaVariant.RED)

        verify(zoneDao).upsert(argThat<InfestationZoneEntity> {
            dominantVariant == "red" && name == "Test Zone"
        })
    }

    @Test
    fun `createZone with null name inserts entity with null name`() = runTest {
        repository.createZone(name = null, dominantVariant = LantanaVariant.PINK)

        verify(zoneDao).upsert(argThat<InfestationZoneEntity> {
            name == null
        })
    }

    @Test
    fun `createZone uses provided status`() = runTest {
        repository.createZone(
            name = "Zone",
            dominantVariant = LantanaVariant.ORANGE,
            status = "underTreatment"
        )

        verify(zoneDao).upsert(argThat<InfestationZoneEntity> {
            status == "underTreatment"
        })
    }

    @Test
    fun `createZone defaults status to active`() = runTest {
        repository.createZone(name = "Zone", dominantVariant = LantanaVariant.WHITE)

        verify(zoneDao).upsert(argThat<InfestationZoneEntity> {
            status == "active"
        })
    }

    @Test
    fun `createZone creates sync queue entry`() = runTest {
        repository.createZone(name = "Zone", dominantVariant = LantanaVariant.PINK)

        verify(syncQueueDao).upsert(argThat<SyncQueueEntity> {
            entityName == "InfestationZone" && operationType == "create"
        })
    }

    @Test
    fun `createZone sets PENDING_CREATE sync status`() = runTest {
        repository.createZone(name = "Zone", dominantVariant = LantanaVariant.PINK)

        verify(zoneDao).upsert(argThat<InfestationZoneEntity> {
            syncStatus == SyncStatus.PENDING_CREATE.value
        })
    }

    @Test
    fun `createZone returns the created entity`() = runTest {
        val entity = repository.createZone(name = "Zone", dominantVariant = LantanaVariant.RED)

        assertEquals("Zone", entity.name)
        assertEquals("red", entity.dominantVariant)
        assertNotNull(entity.id)
        assertTrue(entity.id.isNotEmpty())
    }

    @Test
    fun `createZone generates unique IDs`() = runTest {
        val e1 = repository.createZone(name = "A", dominantVariant = LantanaVariant.PINK)
        val e2 = repository.createZone(name = "B", dominantVariant = LantanaVariant.RED)
        assertNotEquals(e1.id, e2.id)
    }

    @Test
    fun `createZone with UNKNOWN variant stores unknown value`() = runTest {
        val entity = repository.createZone(name = "Zone", dominantVariant = LantanaVariant.UNKNOWN)
        assertEquals("unknown", entity.dominantVariant)
    }

    @Test
    fun `deleteZone calls dao`() = runTest {
        repository.deleteZone("zone-123")
        verify(zoneDao).deleteById("zone-123")
    }

    @Test
    fun `assignSighting calls dao with correct params`() = runTest {
        repository.assignSighting("sighting-1", "zone-1")
        verify(sightingDao).updateZoneAssignment(eq("sighting-1"), eq("zone-1"), any())
    }

    @Test
    fun `assignSighting with null zone removes assignment`() = runTest {
        repository.assignSighting("sighting-1", null)
        verify(sightingDao).updateZoneAssignment(eq("sighting-1"), isNull(), any())
    }
}
