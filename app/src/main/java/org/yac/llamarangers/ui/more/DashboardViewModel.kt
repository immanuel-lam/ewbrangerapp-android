package org.yac.llamarangers.ui.more

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.InfestationZoneDao
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.RangerTaskDao
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.dao.TreatmentRecordDao
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import java.util.Calendar
import javax.inject.Inject

/**
 * Data class for monthly sighting chart entries.
 */
data class MonthlySightingEntry(
    val dateMillis: Long,
    val count: Int,
    val variant: String
)

/**
 * Data class for per-ranger sighting counts.
 */
data class RangerSightingCount(
    val name: String,
    val count: Int
)

/**
 * Ports iOS DashboardViewModel.
 * Aggregates sighting, treatment, zone, and sync statistics.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sightingDao: SightingLogDao,
    private val treatmentDao: TreatmentRecordDao,
    private val zoneDao: InfestationZoneDao,
    private val syncQueueDao: SyncQueueDao,
    private val taskDao: RangerTaskDao,
    private val rangerDao: RangerProfileDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _totalSightings = MutableStateFlow(0)
    val totalSightings: StateFlow<Int> = _totalSightings.asStateFlow()

    private val _sightingsThisMonth = MutableStateFlow(0)
    val sightingsThisMonth: StateFlow<Int> = _sightingsThisMonth.asStateFlow()

    private val _treatmentsThisMonth = MutableStateFlow(0)
    val treatmentsThisMonth: StateFlow<Int> = _treatmentsThisMonth.asStateFlow()

    private val _zoneStatusCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val zoneStatusCounts: StateFlow<Map<String, Int>> = _zoneStatusCounts.asStateFlow()

    private val _monthlySightingData = MutableStateFlow<List<MonthlySightingEntry>>(emptyList())
    val monthlySightingData: StateFlow<List<MonthlySightingEntry>> = _monthlySightingData.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncDate = MutableStateFlow<Long?>(null)
    val lastSyncDate: StateFlow<Long?> = _lastSyncDate.asStateFlow()

    private val _rangerSightingCounts = MutableStateFlow<List<RangerSightingCount>>(emptyList())
    val rangerSightingCounts: StateFlow<List<RangerSightingCount>> = _rangerSightingCounts.asStateFlow()

    private val _clearedZonePercent = MutableStateFlow(0.0)
    val clearedZonePercent: StateFlow<Double> = _clearedZonePercent.asStateFlow()

    private val _openFollowUpTasks = MutableStateFlow(0)
    val openFollowUpTasks: StateFlow<Int> = _openFollowUpTasks.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis

            // Total sightings
            val allSightings = sightingDao.fetchAll()
            _totalSightings.value = allSightings.size

            // Sightings this month
            _sightingsThisMonth.value = sightingDao.fetchSince(startOfMonth).size

            // Treatments this month
            _treatmentsThisMonth.value = treatmentDao.fetchBetween(startOfMonth, now).size

            // Zone status counts
            val zones = zoneDao.fetchAll()
            _zoneStatusCounts.value = zones.groupBy { it.status ?: "unknown" }
                .mapValues { it.value.size }

            // Cleared zone percent
            val cleared = zones.count { it.status == "cleared" }
            _clearedZonePercent.value = if (zones.isEmpty()) 0.0
            else (cleared.toDouble() / zones.size) * 100.0

            // Monthly sighting data (last 6 months)
            buildMonthlySightingData(now)

            // Pending sync count
            _pendingSyncCount.value = syncQueueDao.count()

            // Last sync date
            val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            val lastSync = prefs.getLong("lastSyncTimestamp", -1L)
            _lastSyncDate.value = if (lastSync >= 0) lastSync else null

            // Per-ranger sighting counts
            val rangerMap = rangerDao.fetchAll().associate { it.id to (it.displayName ?: "Unknown") }
            val byRanger = allSightings.groupBy { rangerMap[it.rangerId] ?: "Unknown" }
            _rangerSightingCounts.value = byRanger
                .map { (name, list) -> RangerSightingCount(name, list.size) }
                .sortedByDescending { it.count }

            // Open follow-up tasks (incomplete tasks with a sourceTreatmentId)
            val incompleteTasks = taskDao.fetchIncomplete()
            _openFollowUpTasks.value = incompleteTasks.count { it.sourceTreatmentId != null }
        }
    }

    private suspend fun buildMonthlySightingData(now: Long) {
        val data = mutableListOf<MonthlySightingEntry>()
        val calendar = Calendar.getInstance()

        for (monthOffset in 5 downTo 0) {
            calendar.timeInMillis = now
            calendar.add(Calendar.MONTH, -monthOffset)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis

            val monthSightings = sightingDao.fetchSince(monthStart)
                .filter { it.createdAt < monthEnd }

            val byVariant = monthSightings.groupBy { it.variant }
            for ((variant, sightings) in byVariant) {
                val label = try {
                    InvasiveSpecies.fromValue(variant).displayName
                } catch (_: Exception) {
                    variant.replaceFirstChar { it.uppercase() }
                }
                data.add(MonthlySightingEntry(monthStart, sightings.size, label))
            }
        }
        _monthlySightingData.value = data
    }
}
