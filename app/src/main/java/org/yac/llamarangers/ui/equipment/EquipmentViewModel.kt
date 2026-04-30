package org.yac.llamarangers.ui.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.dao.EquipmentDao
import org.yac.llamarangers.data.local.dao.MaintenanceRecordDao
import org.yac.llamarangers.data.local.entity.EquipmentEntity
import org.yac.llamarangers.data.local.entity.MaintenanceRecordEntity
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val equipmentDao: EquipmentDao,
    private val maintenanceDao: MaintenanceRecordDao
) : ViewModel() {

    private val _equipmentList = MutableStateFlow<List<EquipmentEntity>>(emptyList())
    val equipmentList: StateFlow<List<EquipmentEntity>> = _equipmentList.asStateFlow()

    init {
        loadEquipment()
    }

    fun loadEquipment() {
        viewModelScope.launch {
            _equipmentList.value = equipmentDao.fetchAll()
        }
    }

    fun addEquipment(name: String, type: String, serial: String?, notes: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = EquipmentEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                updatedAt = now,
                name = name,
                equipmentType = type,
                serialNumber = serial,
                notes = notes,
                isActive = true,
                lastMaintenanceDate = null,
                nextMaintenanceDue = null
            )
            equipmentDao.upsert(entity)
            loadEquipment()
        }
    }

    fun addMaintenanceRecord(equipmentId: String, type: String, description: String, performedBy: String, cost: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = MaintenanceRecordEntity(
                id = UUID.randomUUID().toString(),
                equipmentId = equipmentId,
                maintenanceType = type,
                descriptionText = description,
                performedBy = performedBy,
                costAmount = cost,
                date = now
            )
            maintenanceDao.upsert(entity)
            
            // Update equipment's last maintenance date
            val equipment = equipmentDao.findById(equipmentId)
            equipment?.let {
                equipmentDao.upsert(it.copy(
                    lastMaintenanceDate = now,
                    updatedAt = now
                ))
            }
            loadEquipment()
        }
    }
}
