package org.yac.llamarangers.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.ui.theme.RangerOrange
import org.yac.llamarangers.ui.theme.RangerRed

/**
 * Pesticide stock list screen.
 * Ports iOS PesticideListView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesticideListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (pesticideId: String) -> Unit = {},
    viewModel: PesticideViewModel = hiltViewModel()
) {
    val stocks by viewModel.stocks.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supplies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Low stock alert banner
            if (lowStockItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = RangerOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${lowStockItems.size} product(s) low on stock",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(stocks, key = { it.id }) { stock ->
                    StockRow(
                        stock = stock,
                        onClick = { onNavigateToDetail(stock.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddSheet) {
        AddStockBottomSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { name, unit, qty, threshold ->
                viewModel.addStock(name, unit, qty, threshold)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun StockRow(
    stock: PesticideStockEntity,
    onClick: () -> Unit
) {
    val isLow = stock.currentQuantity <= stock.minThreshold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.productName ?: "Unknown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "%.1f %s".format(stock.currentQuantity, stock.unit ?: "L"),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLow) RangerRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLow) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Low stock",
                tint = RangerRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStockBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (productName: String, unit: String, initialQuantity: Double, minThreshold: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var productName by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("litres") }
    var initialQty by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Add Product", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name (e.g. Garlon 600)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Unit selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("litres", "kilograms").forEach { u ->
                    TextButton(
                        onClick = { unit = u },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = u.replaceFirstChar { it.uppercase() },
                            color = if (unit == u) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedTextField(
                value = initialQty,
                onValueChange = { initialQty = it },
                label = { Text("Initial Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("Low-Stock Threshold") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onAdd(
                            productName,
                            unit,
                            initialQty.toDoubleOrNull() ?: 0.0,
                            threshold.toDoubleOrNull() ?: 0.0
                        )
                    },
                    enabled = productName.isNotBlank()
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
