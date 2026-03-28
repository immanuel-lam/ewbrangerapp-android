package org.yac.llamarangers.ui.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * "More" tab menu screen.
 * Ports iOS MoreView from MainTabView.swift.
 */
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Field section
        SectionHeader("Field")
        MenuItem(Icons.AutoMirrored.Filled.MenuBook, "Variant Guide", onNavigateToVariantGuide)
        MenuItem(Icons.Default.Checklist, "Control Protocol", onNavigateToControlProtocol)
        MenuItem(Icons.Default.GridView, "Zones", onNavigateToZoneList)

        // Reports section
        SectionHeader("Reports")
        MenuItem(Icons.Default.BarChart, "Dashboard", onNavigateToDashboard)
        MenuItem(Icons.Default.Science, "Supplies", onNavigateToPesticideList)

        // Device section
        SectionHeader("Device")
        MenuItem(Icons.Default.CellTower, "End of Day Sync", onNavigateToMeshSync)
        MenuItem(Icons.Default.Settings, "Settings", onNavigateToSettings)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}
