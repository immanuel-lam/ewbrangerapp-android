package org.yac.llamarangers.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity
import org.yac.llamarangers.data.repository.PesticideRepository
import javax.inject.Inject

/**
 * Ports iOS PesticideViewModel.
 * Manages pesticide stock list, add stock, log usage, and usage history.
 */
@HiltViewModel
class PesticideViewModel @Inject constructor(
    private val pesticideRepository: PesticideRepository
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<PesticideStockEntity>>(emptyList())
    val stocks: StateFlow<List<PesticideStockEntity>> = _stocks.asStateFlow()

    private val _lowStockItems = MutableStateFlow<List<PesticideStockEntity>>(emptyList())
    val lowStockItems: StateFlow<List<PesticideStockEntity>> = _lowStockItems.asStateFlow()

    private val _usageHistory = MutableStateFlow<List<PesticideUsageRecordEntity>>(emptyList())
    val usageHistory: StateFlow<List<PesticideUsageRecordEntity>> = _usageHistory.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                val allStocks = pesticideRepository.fetchAllStocks()
                _stocks.value = allStocks
                _lowStockItems.value = allStocks.filter { it.currentQuantity <= it.minThreshold }
            } catch (_: Exception) {
                // Keep last-known state
            }
        }
    }

    fun addStock(productName: String, unit: String, initialQuantity: Double, minThreshold: Double) {
        viewModelScope.launch {
            try {
                pesticideRepository.addStock(productName, unit, initialQuantity, minThreshold)
            } catch (_: Exception) { /* best-effort */ }
            load()
        }
    }

    fun logUsage(stockId: String, quantity: Double, notes: String?, rangerId: String) {
        viewModelScope.launch {
            try {
                pesticideRepository.logUsage(stockId, quantity, notes, rangerId)
            } catch (_: Exception) { /* best-effort */ }
            load()
            loadUsageHistory(stockId)
        }
    }

    fun loadUsageHistory(stockId: String) {
        viewModelScope.launch {
            try {
                _usageHistory.value = pesticideRepository.fetchUsageHistory(stockId)
            } catch (_: Exception) {
                // Keep last-known state
            }
        }
    }
}
