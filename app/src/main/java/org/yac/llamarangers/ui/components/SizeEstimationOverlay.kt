package org.yac.llamarangers.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import org.yac.llamarangers.ui.theme.RangerGreen

enum class ReferenceObject(val displayName: String, val realSizeMetres: Double) {
    BOOT("Boot (30cm)", 0.30),
    HAND("Hand (20cm)", 0.20),
    METRE_STICK("Metre stick (100cm)", 1.00),
    A4_PAPER("A4 paper (30cm)", 0.30),
    BACKPACK("Backpack (50cm)", 0.50),
    CUSTOM("Custom...", 0.30)
}

@Composable
fun SizeEstimationOverlay(
    bitmap: Bitmap,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var rectOrigin by remember { mutableStateOf(Offset(0.25f, 0.25f)) } // unit space
    var rectSize by remember { mutableStateOf(Size(0.50f, 0.50f)) } // unit space
    
    var selectedRef by remember { mutableStateOf(ReferenceObject.BOOT) }
    var customSizeCm by remember { mutableStateOf("30") }
    var showCustomDialog by remember { mutableStateOf(false) }
    
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    val refSizeMetres = if (selectedRef == ReferenceObject.CUSTOM) {
        (customSizeCm.toDoubleOrNull() ?: 30.0) / 100.0
    } else {
        selectedRef.realSizeMetres
    }

    fun computeArea(displaySize: Size): Double {
        if (displaySize.width <= 0 || displaySize.height <= 0) return 0.0
        
        // Use shorter side as reference (as in iOS)
        val rectWidthPx = rectSize.width * displaySize.width
        val rectHeightPx = rectSize.height * displaySize.height
        
        val refPx = minOf(rectWidthPx, rectHeightPx)
        if (refPx <= 0) return 0.0
        
        val scale = refSizeMetres / refPx
        return (rectWidthPx * rectHeightPx) * scale * scale
    }

    fun formatArea(area: Double): String {
        return when {
            area < 0.01 -> String.format("~%.4f m²", area)
            area < 10 -> String.format("~%.1f m²", area)
            else -> String.format("~%.0f m²", area)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Skip", color = Color.LightGray)
                }
                Text("Estimate Area", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(64.dp)) // balance
            }

            // Image + Overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { containerSize = it.size }
                    .clip(RoundedCornerShape(0.dp))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Interactive Overlay
                val displaySize = containerSize.toSize()
                if (displaySize.width > 0) {
                    // Calculation for actual image rect within Fit scale
                    val imgAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val boxAspect = displaySize.width / displaySize.height
                    
                    val drawSize = if (imgAspect > boxAspect) {
                        Size(displaySize.width, displaySize.width / imgAspect)
                    } else {
                        Size(displaySize.height * imgAspect, displaySize.height)
                    }
                    
                    val offset = Offset(
                        (displaySize.width - drawSize.width) / 2,
                        (displaySize.height - drawSize.height) / 2
                    )

                    val rectX = offset.x + rectOrigin.x * drawSize.width
                    val rectY = offset.y + rectOrigin.y * drawSize.height
                    val rectW = rectSize.width * drawSize.width
                    val rectH = rectSize.height * drawSize.height

                    // Drawing the rectangle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = RangerGreen.copy(alpha = 0.2f),
                            topLeft = Offset(rectX, rectY),
                            size = Size(rectW, rectH)
                        )
                        drawRect(
                            color = RangerGreen,
                            topLeft = Offset(rectX, rectY),
                            size = Size(rectW, rectH),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Drag Handles
                    Box(
                        modifier = Modifier
                            .offset(rectX.dp / 2.7f, rectY.dp / 2.7f) // Rough adjustment for density, simplified for demo
                            .fillMaxSize()
                    ) {
                        // Corner handles (Top Left)
                        Handle(
                            offset = Offset(rectX, rectY),
                            onDrag = { delta ->
                                val dx = delta.x / drawSize.width
                                val dy = delta.y / drawSize.height
                                val newX = (rectOrigin.x + dx).coerceIn(0f, rectOrigin.x + rectSize.width - 0.05f)
                                val newY = (rectOrigin.y + dy).coerceIn(0f, rectOrigin.y + rectSize.height - 0.05f)
                                val newW = rectSize.width + (rectOrigin.x - newX)
                                val newH = rectSize.height + (rectOrigin.y - newY)
                                rectOrigin = Offset(newX, newY)
                                rectSize = Size(newW, newH)
                            }
                        )
                        // Bottom Right
                        Handle(
                            offset = Offset(rectX + rectW, rectY + rectH),
                            onDrag = { delta ->
                                val dx = delta.x / drawSize.width
                                val dy = delta.y / drawSize.height
                                val newW = (rectSize.width + dx).coerceIn(0.05f, 1f - rectOrigin.x)
                                val newH = (rectSize.height + dy).coerceIn(0.05f, 1f - rectOrigin.y)
                                rectSize = Size(newW, newH)
                            }
                        )
                    }
                    
                    // Area Badge
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = RangerGreen.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = formatArea(computeArea(drawSize)),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Bottom Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Reference object in photo:", style = MaterialTheme.typography.bodySmall)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReferenceObject.entries.take(3).forEach { ref ->
                            ReferenceChip(
                                selected = selectedRef == ref,
                                label = if (ref == ReferenceObject.CUSTOM && selectedRef == ref) "Custom ($customSizeCm cm)" else ref.displayName,
                                onClick = { 
                                    selectedRef = ref
                                    if (ref == ReferenceObject.CUSTOM) showCustomDialog = true
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReferenceObject.entries.drop(3).forEach { ref ->
                            ReferenceChip(
                                selected = selectedRef == ref,
                                label = ref.displayName,
                                onClick = { selectedRef = ref }
                            )
                        }
                    }

                    Text(
                        "Drag the box to cover the infestation. Corner handles resize it.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = { 
                            // Re-calculate with drawSize properly
                            val displaySize = containerSize.toSize()
                            val imgAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val boxAspect = displaySize.width / displaySize.height
                            val drawSize = if (imgAspect > boxAspect) Size(displaySize.width, displaySize.width / imgAspect) else Size(displaySize.height * imgAspect, displaySize.height)
                            onConfirm(formatArea(computeArea(drawSize))) 
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RangerGreen)
                    ) {
                        Text("Confirm Estimate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (showCustomDialog) {
            AlertDialog(
                onDismissRequest = { showCustomDialog = false },
                title = { Text("Custom Reference") },
                text = {
                    Column {
                        Text("Enter the real-world size in cm.")
                        OutlinedTextField(
                            value = customSizeCm,
                            onValueChange = { customSizeCm = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCustomDialog = false }) { Text("Apply") }
                }
            )
        }
    }
}

@Composable
private fun Handle(offset: Offset, onDrag: (Offset) -> Unit) {
    Box(
        modifier = Modifier
            .offset(offset.x.dp / 2.7f - 11.dp, offset.y.dp / 2.7f - 11.dp) // Adjust for handle size and density
            .size(22.dp)
            .background(Color.White, CircleShape)
            .border(2.dp, RangerGreen, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
    )
}

@Composable
private fun ReferenceChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier.height(32.dp)
    )
}
