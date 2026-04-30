package org.yac.llamarangers.ui.sighting

import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.repository.SightingRepository
import org.yac.llamarangers.domain.model.enums.InfestationSize
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.location.LocationManager
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LogSightingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var locationManager: LocationManager
    private lateinit var sightingRepository: SightingRepository
    private lateinit var authManager: AuthManager

    private fun fakeLocation(lat: Double = -14.7019, lon: Double = 143.7075, acc: Float = 5f): Location {
        val loc = mock<Location>()
        whenever(loc.latitude).thenReturn(lat)
        whenever(loc.longitude).thenReturn(lon)
        whenever(loc.accuracy).thenReturn(acc)
        return loc
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        locationManager = mock()
        sightingRepository = mock()
        authManager = mock()

        // Default stubs
        whenever(locationManager.accuracyLevel).thenReturn(MutableStateFlow(LocationManager.AccuracyLevel.GOOD))
        whenever(authManager.currentRangerId).thenReturn(MutableStateFlow(UUID.randomUUID()))
        whenever(authManager.isAuthenticated).thenReturn(MutableStateFlow(true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubLocationCapture(location: Location = fakeLocation()) {
        runTest { whenever(locationManager.captureLocation()).thenReturn(location) }
    }

    private fun buildViewModel(): LogSightingViewModel {
        stubLocationCapture()
        return LogSightingViewModel(locationManager, sightingRepository, authManager)
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `init captures location automatically`() = runTest {
        val vm = buildViewModel()
        assertNotNull(vm.capturedLocation.value)
    }

    @Test
    fun `canSave is false when no variant selected`() = runTest {
        val vm = buildViewModel()
        assertFalse(vm.canSave)
    }

    @Test
    fun `canSave is true when location and variant both set`() = runTest {
        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.PINK)
        assertTrue(vm.canSave)
    }

    // ── Variant selection ────────────────────────────────────────────────────

    @Test
    fun `setSelectedVariant updates state`() {
        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.RED)
        assertEquals(LantanaVariant.RED, vm.selectedVariant.value)
    }

    @Test
    fun `setSelectedVariant to null clears selection`() {
        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.RED)
        vm.setSelectedVariant(null)
        assertNull(vm.selectedVariant.value)
    }

    // ── Size selection ───────────────────────────────────────────────────────

    @Test
    fun `default size is SMALL`() {
        val vm = buildViewModel()
        assertEquals(InfestationSize.SMALL, vm.selectedSize.value)
    }

    @Test
    fun `setSelectedSize updates state`() {
        val vm = buildViewModel()
        vm.setSelectedSize(InfestationSize.LARGE)
        assertEquals(InfestationSize.LARGE, vm.selectedSize.value)
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    @Test
    fun `setNotes updates state`() {
        val vm = buildViewModel()
        vm.setNotes("Found near creek bed")
        assertEquals("Found near creek bed", vm.notes.value)
    }

    // ── Photos ───────────────────────────────────────────────────────────────

    @Test
    fun `addPhoto appends to list`() {
        val vm = buildViewModel()
        vm.addPhoto("photo_1.jpg")
        vm.addPhoto("photo_2.jpg")
        assertEquals(listOf("photo_1.jpg", "photo_2.jpg"), vm.photoFilenames.value)
    }

    // ── Control recommendation ───────────────────────────────────────────────

    @Test
    fun `controlRecommendation is null when no variant selected`() {
        val vm = buildViewModel()
        assertNull(vm.controlRecommendation)
    }

    @Test
    fun `controlRecommendation returns methods for selected variant`() {
        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.RED)
        val rec = vm.controlRecommendation
        assertNotNull(rec)
        assertTrue(rec!!.contains("Cut Stump"))
        assertTrue(rec.contains("Basal Bark"))
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    @Test
    fun `save does nothing when canSave is false`() = runTest {
        val vm = buildViewModel()
        // No variant selected → canSave = false
        vm.save()
        assertFalse(vm.didSave.value)
        assertFalse(vm.isSaving.value)
        verifyNoInteractions(sightingRepository)
    }

    @Test
    fun `save sets error when not authenticated`() = runTest {
        whenever(authManager.currentRangerId).thenReturn(MutableStateFlow<UUID?>(null))
        whenever(authManager.isAuthenticated).thenReturn(MutableStateFlow(false))

        stubLocationCapture()
        val vm = LogSightingViewModel(locationManager, sightingRepository, authManager)
        vm.setSelectedVariant(LantanaVariant.PINK)

        vm.save()

        assertEquals("Not authenticated. Please log in again.", vm.saveError.value)
        assertFalse(vm.didSave.value)
        verifyNoInteractions(sightingRepository)
    }

    @Test
    fun `save calls repository and sets didSave on success`() = runTest {
        val entity = mock<SightingLogEntity>()
        whenever(sightingRepository.createSighting(
            any(), any(), any(), any(), any(), anyOrNull(), any(), any(), any()
        )).thenReturn(entity)

        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.PINK)
        vm.save()

        assertTrue(vm.didSave.value)
        assertFalse(vm.isSaving.value)
        assertNull(vm.saveError.value)
    }

    @Test
    fun `save sets error on repository exception`() = runTest {
        whenever(sightingRepository.createSighting(
            any(), any(), any(), any(), any(), anyOrNull(), any(), any(), any()
        )).thenThrow(RuntimeException("DB write failed"))

        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.PINK)
        vm.save()

        assertFalse(vm.didSave.value)
        assertFalse(vm.isSaving.value)
        assertEquals("DB write failed", vm.saveError.value)
    }

    @Test
    fun `save passes correct data to repository`() = runTest {
        val rangerId = UUID.randomUUID()
        whenever(authManager.currentRangerId).thenReturn(MutableStateFlow<UUID?>(rangerId))

        val location = fakeLocation(lat = -14.5, lon = 143.5, acc = 8f)
        stubLocationCapture(location)

        val entity = mock<SightingLogEntity>()
        whenever(sightingRepository.createSighting(
            any(), any(), any(), any(), any(), anyOrNull(), any(), any(), any()
        )).thenReturn(entity)

        val vm = LogSightingViewModel(locationManager, sightingRepository, authManager)
        vm.setSelectedVariant(LantanaVariant.ORANGE)
        vm.setSelectedSize(InfestationSize.LARGE)
        vm.setNotes("Near creek")
        vm.addPhoto("img.jpg")
        vm.save()

        verify(sightingRepository).createSighting(
            latitude = eq(-14.5),
            longitude = eq(143.5),
            horizontalAccuracy = eq(8.0),
            variant = eq(LantanaVariant.ORANGE),
            infestationSize = eq(InfestationSize.LARGE),
            notes = eq("Near creek"),
            photoFilenames = eq(listOf("img.jpg")),
            rangerId = eq(rangerId.toString()),
            deviceId = eq("android")
        )
    }

    @Test
    fun `save with blank notes passes null`() = runTest {
        val entity = mock<SightingLogEntity>()
        whenever(sightingRepository.createSighting(
            any(), any(), any(), any(), any(), anyOrNull(), any(), any(), any()
        )).thenReturn(entity)

        val vm = buildViewModel()
        vm.setSelectedVariant(LantanaVariant.PINK)
        vm.setNotes("   ")  // blank
        vm.save()

        verify(sightingRepository).createSighting(
            any(), any(), any(), any(), any(),
            notes = isNull(),
            any(), any(), any()
        )
    }

    // ── Recapture ────────────────────────────────────────────────────────────

    @Test
    fun `recaptureLocation re-acquires location`() = runTest {
        val vm = buildViewModel()
        assertNotNull(vm.capturedLocation.value)

        vm.recaptureLocation()
        // With UnconfinedTestDispatcher, the coroutine completes immediately
        assertNotNull(vm.capturedLocation.value)
    }
}
