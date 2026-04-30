package org.yac.llamarangers.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.repository.ZoneRepository
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.ui.components.VariantColourDot

/**
 * Form for adding a new infestation zone.
 * Ports iOS AddZoneView.
 * OutlinedTextField for name, ExposedDropdownMenuBox for variant and status.
 * Proper M3 spacing (16dp gaps between fields).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddZoneScreen(
    zoneRepository: ZoneRepository,
    onNavigateBack: () -> Unit = {}
) {
    var zoneName by remember { mutableStateOf("") }
    var selectedVariant by remember { mutableStateOf(InvasiveSpecies.UNKNOWN) }
    var selectedStatus by remember { mutableStateOf("active") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var variantExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val statuses = listOf(
        "active" to "Active",
        "underTreatment" to "Under Treatment",
        "cleared" to "Cleared"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Zone") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            saveError = null
                            isSaving = true
                            scope.launch {
                                try {
                                    zoneRepository.createZone(
                                        name = zoneName.ifBlank { null },
                                        dominantVariant = selectedVariant,
                                        status = selectedStatus
                                    )
                                    onNavigateBack()
                                } catch (e: Exception) {
                                    saveError = e.localizedMessage ?: "Failed to save zone"
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Zone Details",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Error message
            saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Zone name field
            OutlinedTextField(
                value = zoneName,
                onValueChange = { zoneName = it },
                label = { Text("Zone Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Variant picker
            ExposedDropdownMenuBox(
                expanded = variantExpanded,
                onExpandedChange = { variantExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedVariant.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dominant Variant") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = variantExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = variantExpanded,
                    onDismissRequest = { variantExpanded = false }
                ) {
                    InvasiveSpecies.entries.forEach { v ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    VariantColourDot(variant = v, size = 10.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(v.displayName)
                                }
                            },
                            onClick = {
                                selectedVariant = v
                                variantExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status picker
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = statuses.first { it.first == selectedStatus }.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statuses.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedStatus = value
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
