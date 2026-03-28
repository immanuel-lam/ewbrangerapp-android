package org.yac.llamarangers.ui.more

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity
import org.yac.llamarangers.ui.components.LargeButton
import org.yac.llamarangers.ui.theme.RangerRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pesticide detail screen showing stock info and usage history.
 * Ports iOS PesticideDetailView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideDetailScreen(
    pesticideId: String,
    rangerId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToLogUsage: () -> Unit = {},
    viewModel: PesticideViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val usageHistory by viewModel.usageHistory.collectAsStateWithLifecycle()
    val stock = stocks.find { it.id == pesticideId }

    var showLogUsage by remember { mutableStateOf(false) }

    LaunchedEffect(pesticideId) {
        viewModel.load()
        viewModel.loadUsageHistory(pesticideId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock?.productName ?: "Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (stock == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Product not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary card
            SummaryCard(stock)

            // Log Usage button
            LargeButton(
                title = "Log Usage",
                onClick = { showLogUsage = true }
            )

            // Usage history
            Text("Usage History", style = MaterialTheme.typography.titleMedium)

            if (usageHistory.isEmpty()) {
                Text(
                    "No usage recorded yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                usageHistory.forEach { record ->
                    UsageRow(record = record, unit = stock.unit ?: "L")
                }
            }
        }
    }

    if (showLogUsage) {
        LogUsageBottomSheet(
            stock = stock!!,
            onDismiss = { showLogUsage = false },
            onSave = { quantity, notes ->
                viewModel.logUsage(pesticideId, quantity, notes, rangerId)
                showLogUsage = false
            }
        )
    }
}

@Composable
private fun SummaryCard(stock: PesticideStockEntity) {
    val isLow = stock.currentQuantity <= stock.minThreshold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%.1f %s".format(stock.currentQuantity, stock.unit ?: "L"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLow) RangerRed else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Min: %.1f %s".format(stock.minThreshold, stock.unit ?: "L"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLow) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = RangerRed,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    "Low Stock \u2014 reorder required",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RangerRed
                )
            }
        }
    }
}

@Composable
private fun UsageRow(record: PesticideUsageRecordEntity, unit: String) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "\u2013%.1f %s".format(record.usedQuantity, unit),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RangerRed
            )
            if (!record.notes.isNullOrBlank()) {
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        record.usedAt?.let { usedAt ->
            Text(
                text = dateFormat.format(Date(usedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
