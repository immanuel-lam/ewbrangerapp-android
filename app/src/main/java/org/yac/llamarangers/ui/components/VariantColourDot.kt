package org.yac.llamarangers.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yac.llamarangers.domain.model.enums.LantanaVariant

/**
 * Filled circle colour-coded to a Lantana variant.
 * Ports iOS VariantColourDot. No border — the filled colour speaks for itself.
 */
@Composable
fun VariantColourDot(
    variant: LantanaVariant,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(variant.color)
    )
}
