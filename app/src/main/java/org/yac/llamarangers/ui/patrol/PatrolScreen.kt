package org.yac.llamarangers.ui.patrol

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.resources.PortStewartZones

/**
 * Main Patrol screen. Shows active patrol or start new patrol + history.
 * Ports iOS PatrolView.
 * Area picker uses ExposedDropdownMenuBox; "Start Patrol" is a full-width FilledButton.
 * History toggle uses SingleChoiceSegmentedButtonRow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrolScreen(
    viewModel: PatrolViewModel = hiltViewModel()
) {
    val activePatrol by viewModel.activePatrol.collectAsState()
    val patrols by viewModel.patrols.collectAsState()
    val selectedArea by viewModel.selectedAreaName.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Patrol") })
        }
    ) { padding ->
        if (activePatrol != null) {
            ActivePatrolScreen(viewModel = viewModel)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start patrol card
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Start New Patrol",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        AreaPicker(
                            areas = PortStewartZones.patrolAreas,
                            selectedArea = selectedArea,
                            onAreaSelected = { viewModel.setSelectedArea(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full-width FilledButton (M3 default Button is filled)
                        Button(
                            onClick = { viewModel.startPatrol() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Start Patrol",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // History toggle
                var historyTab by remember { mutableIntStateOf(0) }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = historyTab == 0,
                        onClick = { historyTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("List") }
                    SegmentedButton(
                        selected = historyTab == 1,
                        onClick = { historyTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Calendar") }
                }

                if (historyTab == 0) {
                    PatrolListContent(patrols = patrols)
                } else {
                    PatrolCalendarContent(patrols = patrols)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AreaPicker(
    areas: List<String>,
    selectedArea: String,
    onAreaSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedArea,
            onValueChange = {},
            readOnly = true,
            label = { Text("Area") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            areas.forEach { area ->
                DropdownMenuItem(
                    text = { Text(area) },
                    onClick = {
                        onAreaSelected(area)
                        expanded = false
                    }
                )
            }
        }
    }
}
