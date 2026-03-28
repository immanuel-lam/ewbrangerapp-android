package org.yac.llamarangers.ui.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.resources.PortStewartZones
import org.yac.llamarangers.ui.components.LargeButton

/**
 * Main Patrol screen. Shows active patrol or start new patrol + history.
 * Ports iOS PatrolView.
 */
@Composable
fun PatrolScreen(
    viewModel: PatrolViewModel = hiltViewModel()
) {
    val activePatrol by viewModel.activePatrol.collectAsState()
    val patrols by viewModel.patrols.collectAsState()
    val selectedArea by viewModel.selectedAreaName.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Patrol",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        if (activePatrol != null) {
            ActivePatrolScreen(viewModel = viewModel)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Start patrol section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text("Start New Patrol", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    AreaPicker(
                        areas = PortStewartZones.patrolAreas,
                        selectedArea = selectedArea,
                        onAreaSelected = { viewModel.setSelectedArea(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LargeButton(
                        title = "Start Patrol",
                        onClick = { viewModel.startPatrol() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // History toggle
                var historyTab by remember { mutableIntStateOf(0) }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
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

                Spacer(modifier = Modifier.height(16.dp))

                if (historyTab == 0) {
                    PatrolListContent(patrols = patrols)
                } else {
                    PatrolCalendarContent(patrols = patrols)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AreaPicker(
    areas: List<String>,
    selectedArea: String,
    onAreaSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = selectedArea,
            onValueChange = {},
            readOnly = true,
            label = { Text("Area") },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
        )
        this.ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            areas.forEach { area ->
                androidx.compose.material3.DropdownMenuItem(
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
