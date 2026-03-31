package org.yac.llamarangers.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.LantanaVariant

/**
 * Unit tests for SightingRepository.
 * Note: createSighting uses Room's withTransaction which requires an integration test
 * (real DB) to test properly. These tests cover non-transactional operations.
 */
class SightingRepositoryTest {

    private lateinit var sightingDao: SightingLogDao

    @Before
    fun setUp() {
        sightingDao = mock()
    }

    @Test
    fun `deleteSighting calls dao with correct ID`() = runTest {
        val repo = buildRepo()
        repo.deleteSighting("sighting-123")
        verify(sightingDao).deleteById("sighting-123")
    }

    @Test
    fun `fetchAllSightings delegates to dao`() = runTest {
        whenever(sightingDao.fetchAll()).thenReturn(emptyList())
        val repo = buildRepo()
        val result = repo.fetchAllSightings()
        assertTrue(result.isEmpty())
        verify(sightingDao).fetchAll()
    }

    @Test
    fun `fetchSightingsSince delegates to dao`() = runTest {
        whenever(sightingDao.fetchSince(any())).thenReturn(emptyList())
        val repo = buildRepo()
        val result = repo.fetchSightingsSince(1000L)
        verify(sightingDao).fetchSince(1000L)
    }

    private fun buildRepo(): SightingRepository {
        return SightingRepository(
            db = mock(),
            sightingDao = sightingDao,
            rangerDao = mock(),
            syncQueueDao = mock()
        )
    }
}
