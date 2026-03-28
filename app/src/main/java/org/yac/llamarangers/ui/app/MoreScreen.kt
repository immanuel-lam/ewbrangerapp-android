package org.yac.llamarangers.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

/**
 * "More" tab menu screen.
 * Ports iOS MoreView from MainTabView.swift.
 * Sections: Field Tools, Operations, Sync & Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToVariantGuide: () -> Unit = {},
    onNavigateToControlProtocol: () -> Unit = {},
    onNavigateToZoneList: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToPesticideList: () -> Unit = {},
    onNavigateToMeshSync: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("More") })
        }
    ) { padding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Field Tools section
        item { MoreSectionHeader("Field Tools") }
        item {
            MoreListItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "Guide",
                onClick = onNavigateToVariantGuide
            )
        }
        item {
            MoreListItem(
                icon = Icons.Default.Checklist,
                label = "Protocol",
                onClick = onNavigateToControlProtocol
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // Operations section
        item { MoreSectionHeader("Operations") }
        item {
            MoreListItem(
                icon = Icons.Default.GridView,
                label = "Zones",
                onClick = onNavigateToZoneList
            )
        }
        item {
            MoreListItem(
                icon = Icons.Default.BarChart,
                label = "Dashboard",
                onClick = onNavigateToDashboard
            )
        }
        item {
            MoreListItem(
                icon = Icons.Default.Science,
                label = "Supplies",
                onClick = onNavigateToPesticideList
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

        // Sync & Settings section
        item { MoreSectionHeader("Sync & Settings") }
        item {
            MoreListItem(
                icon = Icons.Default.CellTower,
                label = "Sync",
                onClick = onNavigateToMeshSync
            )
        }
        item {
            MoreListItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onNavigateToSettings
            )
        }
    }
    }
}

@Composable
private fun MoreSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp, end = 16.dp)
    )
}

@Composable
private fun MoreListItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
