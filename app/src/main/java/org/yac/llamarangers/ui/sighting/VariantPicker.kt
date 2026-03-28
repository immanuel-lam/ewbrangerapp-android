package org.yac.llamarangers.ui.sighting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.LantanaVariant

/**
 * Horizontal scrolling variant picker.
 * Ports iOS VariantPickerView.
 */
@Composable
fun VariantPicker(
    selectedVariant: LantanaVariant?,
    onVariantSelected: (LantanaVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Variant",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 2.dp)
        ) {
            LantanaVariant.entries.forEach { variant ->
                VariantCard(
                    variant = variant,
                    isSelected = selectedVariant == variant,
                    onClick = { onVariantSelected(variant) }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
private fun VariantCard(
    variant: LantanaVariant,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Colour swatch
        Spacer(
            modifier = Modifier
                .width(70.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(variant.color)
                .then(
                    if (isSelected) Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface,
                        RoundedCornerShape(8.dp)
                    ) else Modifier
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = variant.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(30.dp)
        )
    }
}
