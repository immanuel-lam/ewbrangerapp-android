package org.yac.llamarangers.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.ui.components.VariantColourDot

/**
 * Variant guide list screen.
 * Ports iOS VariantGuideView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantGuideScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (variantValue: String) -> Unit = {}
) {
    val variants = LantanaVariant.entries.filter { it != LantanaVariant.UNKNOWN }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Variant Guide") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(variants) { variant ->
                Row(
                    modifier = Modifier
                        .clickable { onNavigateToDetail(variant.value) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VariantColourDot(variant = variant, size = 20.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = variant.displayName,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = variant.controlMethods.joinToString(", ") { it.displayName },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
