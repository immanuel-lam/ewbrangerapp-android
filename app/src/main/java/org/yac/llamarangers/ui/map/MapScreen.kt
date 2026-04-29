package org.yac.llamarangers.ui.map

import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.ui.theme.RangerGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Main map screen with osmdroid MapView, zone overlays, draw mode, layer toggles, timeline, and FAB.
 * Ports iOS MapContainerView + MapView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateToLogSighting: () -> Unit = {},
    onNavigateToSightingDetail: (String) -> Unit = {},
    onNavigateToAddZone: () -> Unit = {},
    onNavigateToZoneDetail: (String) -> Unit = {},
    onNavigateToEditZone: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sightings by viewModel.sightings.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val patrols by viewModel.patrols.collectAsState()
    val zoneSnapshots by viewModel.zoneSnapshots.collectAsState()
    val zoneSightings by viewModel.zoneSightings.collectAsState()
    val mapType by viewModel.mapType.collectAsState()
    val showSightings by viewModel.showSightings.collectAsState()
    val showZones by viewModel.showZones.collectAsState()
    val showPatrols by viewModel.showPatrols.collectAsState()
    val timelineDate by viewModel.timelineDate.collectAsState()
    val isPlaying by viewModel.isPlayingTimeline.collectAsState()
    val isDrawing by viewModel.isDrawing.collectAsState()
    val drawingZone by viewModel.drawingZone.collectAsState()
    val drawVertices by viewModel.drawVertices.collectAsState()
    val showZonePicker by viewModel.showZonePicker.collectAsState()

    var showTimeline by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showMapTypeMenu by remember { mutableStateOf(false) }
    var showBloomCalendar by remember { mutableStateOf(false) }
    var actionCardData by remember { mutableStateOf<MapActionCardData?>(null) }

    // Configure osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // osmdroid MapView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(-14.7, 143.7))
                    zoomController.setVisibility(
                        org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
                    )
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // --- Map tap events overlay (must be first for draw mode) ---
                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (isDrawing) {
                            viewModel.addVertex(p.latitude, p.longitude)
                            return true
                        }
                        // Zone hit-test for tap on zone overlays
                        // (Polygon overlays handle their own click via setOnClickListener)
                        // Dismiss action card on map tap
                        actionCardData = null
                        return false
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                })
                mapView.overlays.add(eventsOverlay)

                // --- Zone overlays ---
                if (showZones && !isDrawing) {
                    for (zone in zones) {
                        val snapshot = zoneSnapshots[zone.id]
                        val status = zone.status ?: "active"

                        if (snapshot != null && snapshot.polygonCoordinates.size >= 3) {
                            // Polygon overlay from snapshot
                            val polygon = Polygon(mapView).apply {
                                val coords = snapshot.polygonCoordinates
                                points = coords.map { GeoPoint(it[0], it[1]) }
                                fillPaint.color = zoneStatusFillColor(status)
                                outlinePaint.color = zoneStatusStrokeColor(status)
                                outlinePaint.strokeWidth = 3f
                                title = zone.name ?: "Zone"
                                setOnClickListener { _, _, _ ->
                                    actionCardData = buildZoneActionCard(
                                        zone,
                                        onNavigateToZoneDetail,
                                        onNavigateToEditZone,
                                        viewModel
                                    )
                                    true
                                }
                            }
                            mapView.overlays.add(polygon)
                        } else {
                            // Circle fallback from sighting centroid
                            val sightingCoords = zoneSightings[zone.id]
                            if (sightingCoords != null && sightingCoords.isNotEmpty()) {
                                val centroidLat =
                                    sightingCoords.sumOf { it.first } / sightingCoords.size
                                val centroidLon =
                                    sightingCoords.sumOf { it.second } / sightingCoords.size
                                val centroid = GeoPoint(centroidLat, centroidLon)

                                val distances = sightingCoords.map { (lat, lon) ->
                                    haversineDistance(centroidLat, centroidLon, lat, lon)
                                }
                                val radius = maxOf((distances.maxOrNull() ?: 0.0) + 50.0, 100.0)

                                val circle = Polygon(mapView).apply {
                                    points = Polygon.pointsAsCircle(centroid, radius)
                                    fillPaint.color = zoneStatusFillColor(status)
                                    outlinePaint.color = zoneStatusStrokeColor(status)
                                    outlinePaint.strokeWidth = 3f
                                    title = zone.name ?: "Zone"
                                    setOnClickListener { _, _, _ ->
                                        actionCardData = buildZoneActionCard(
                                            zone,
                                            onNavigateToZoneDetail,
                                            onNavigateToEditZone,
                                            viewModel
                                        )
                                        true
                                    }
                                }
                                mapView.overlays.add(circle)
                            }
                        }
                    }
                }

                // --- Sighting markers ---
                val visibleSightings = if (showSightings) {
                    sightings.filter { it.createdAt <= timelineDate }
                } else emptyList()
                visibleSightings.forEach { sighting ->
                        val variant = InvasiveSpecies.fromValue(sighting.variant)
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(sighting.latitude, sighting.longitude)
                            title = variant.displayName
                            snippet = sighting.infestationSize
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            // Color-code by variant
                            val pinDrawable = createColoredPinDrawable(
                                context,
                                variant.color.toArgb()
                            )
                            icon = pinDrawable

                            setOnMarkerClickListener { _, _ ->
                                if (!isDrawing) {
                                    actionCardData = MapActionCardData(
                                        title = variant.displayName,
                                        subtitle = sighting.infestationSize,
                                        sightingId = sighting.id,
                                        actions = listOf(
                                            MapCardAction(
                                                "View Details",
                                                false,
                                                Icons.Default.Info
                                            ) {
                                                onNavigateToSightingDetail(sighting.id)
                                            },
                                            MapCardAction(
                                                "Delete",
                                                true,
                                                Icons.Default.Delete
                                            ) {
                                                viewModel.deleteSighting(sighting.id)
                                            }
                                        )
                                    )
                                }
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }

                // --- Patrol markers ---
                if (showPatrols) {
                    viewModel.filteredPatrols.forEach { patrolData ->
                        val isActive = patrolData.patrol.endTime == null
                        val marker = Marker(mapView).apply {
                            position =
                                GeoPoint(patrolData.latitude, patrolData.longitude)
                            title = patrolData.patrol.areaName ?: "Patrol"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            // Blue for active, purple for completed
                            val patrolColor = if (isActive) {
                                AndroidColor.argb(255, 33, 150, 243) // blue
                            } else {
                                AndroidColor.argb(255, 156, 39, 176) // purple
                            }
                            icon = createColoredPinDrawable(context, patrolColor)

                            setOnMarkerClickListener { _, _ ->
                                if (!isDrawing) {
                                    val subtitle = if (isActive) "Active" else "Completed"
                                    actionCardData = MapActionCardData(
                                        title = patrolData.patrol.areaName ?: "Patrol",
                                        subtitle = subtitle,
                                        actions = listOf(
                                            MapCardAction(
                                                "Delete",
                                                true,
                                                Icons.Default.Delete
                                            ) {
                                                viewModel.deletePatrol(patrolData.patrol.id)
                                            }
                                        )
                                    )
                                }
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }

                // --- Draw mode overlays ---
                if (isDrawing && drawVertices.isNotEmpty()) {
                    // Dashed preview polyline
                    if (drawVertices.size >= 2) {
                        val polyline = Polyline(mapView).apply {
                            setPoints(drawVertices.map { GeoPoint(it.first, it.second) })
                            outlinePaint.color = AndroidColor.YELLOW
                            outlinePaint.strokeWidth = 3f
                            outlinePaint.pathEffect =
                                DashPathEffect(floatArrayOf(10f, 10f), 0f)
                        }
                        mapView.overlays.add(polyline)
                    }

                    // Vertex markers (numbered yellow pins)
                    drawVertices.forEachIndexed { index, vertex ->
                        val vertexMarker = Marker(mapView).apply {
                            position = GeoPoint(vertex.first, vertex.second)
                            title = "${index + 1}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            // Use a yellow circle with the vertex number
                            icon = createNumberedPinDrawable(
                                context,
                                AndroidColor.YELLOW,
                                index + 1
                            )
                        }
                        mapView.overlays.add(vertexMarker)
                    }
                }

                mapView.invalidate()
            }
        )

        // --- UI Overlays (only when NOT drawing) ---
        if (!isDrawing) {
            // Top overlay row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bloom calendar button
                Button(
                    onClick = { showBloomCalendar = true },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RangerGreen)
                ) {
                    Icon(
                        Icons.Default.Map, // Closest to leaf.fill
                        contentDescription = "Bloom",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bloom", style = MaterialTheme.typography.labelMedium)
                }

                // Map type picker
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .clickable { showMapTypeMenu = true }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Map Type",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (mapType) {
                                MapViewModel.MapType.SATELLITE -> "Satellite"
                                MapViewModel.MapType.HYBRID -> "Hybrid"
                                MapViewModel.MapType.STANDARD -> "Standard"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    DropdownMenu(
                        expanded = showMapTypeMenu,
                        onDismissRequest = { showMapTypeMenu = false }
                    ) {
                        MapViewModel.MapType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        type.name.lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    viewModel.setMapType(type)
                                    showMapTypeMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Left side: layer toggle stack
            LayerTogglePanel(
                showSightings = showSightings,
                showZones = showZones,
                showPatrols = showPatrols,
                onToggleSightings = { viewModel.toggleSightings() },
                onToggleZones = { viewModel.toggleZones() },
                onTogglePatrols = { viewModel.togglePatrols() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 80.dp)
            )

            // Bottom bar: timeline + FAB
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                AnimatedVisibility(
                    visible = showTimeline,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    TimelineScrubber(
                        date = timelineDate,
                        range = viewModel.dateRange,
                        isPlaying = isPlaying,
                        onDateChange = { viewModel.setTimelineDate(it) },
                        onTogglePlay = { viewModel.toggleTimeline() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeline toggle button
                    IconButton(
                        onClick = { showTimeline = !showTimeline },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Timeline",
                            tint = if (showTimeline) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // FAB with menu
                    Box {
                        FloatingActionButton(
                            onClick = { showFabMenu = true },
                            containerColor = RangerGreen,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        DropdownMenu(
                            expanded = showFabMenu,
                            onDismissRequest = { showFabMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Log Sighting") },
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToLogSighting()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Zone") },
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToAddZone()
                                }
                            )
                            if (zones.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Draw Zone Boundary") },
                                    onClick = {
                                        showFabMenu = false
                                        viewModel.requestDrawMode()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Draw mode banner ---
        AnimatedVisibility(
            visible = isDrawing,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DrawModeBanner(
                zoneName = drawingZone?.name ?: "Zone",
                vertexCount = drawVertices.size,
                canUndo = drawVertices.isNotEmpty(),
                canSave = drawVertices.size >= 3,
                onUndo = { viewModel.undoVertex() },
                onCancel = { viewModel.cancelDraw() },
                onSave = { viewModel.savePolygon() }
            )
        }

        // --- Floating action card ---
        actionCardData?.let { card ->
            MapActionCard(
                data = card,
                onDismiss = { actionCardData = null }
            )
        }

        // --- Zone picker bottom sheet ---
        if (showZonePicker) {
            ZonePickerBottomSheet(
                zones = zones,
                onSelect = { zone -> viewModel.enterDrawMode(zone) },
                onDismiss = { viewModel.dismissZonePicker() }
            )
        }
    }
}

// --- Draw Mode Banner ---

@Composable
private fun DrawModeBanner(
    zoneName: String,
    vertexCount: Int,
    canUndo: Boolean,
    canSave: Boolean,
    onUndo: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Info row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Drawing: $zoneName",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = "$vertexCount vertices \u2014 tap map to add points",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            if (canUndo) {
                OutlinedButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }
            }
        }

        // Cancel / Save row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RangerGreen,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Save Polygon", color = Color.White)
            }
        }
    }
}

// --- Zone Picker Bottom Sheet ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonePickerBottomSheet(
    zones: List<InfestationZoneEntity>,
    onSelect: (InfestationZoneEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Zone to Draw",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            HorizontalDivider()
            LazyColumn {
                items(zones) { zone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(zone) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot for dominant variant
                        val variant = InvasiveSpecies.fromValue(zone.dominantVariant ?: "")
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(variant.color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = zone.name ?: "Unnamed Zone",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = statusLabel(zone.status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

// --- Helper functions ---

/** Build a MapActionCardData for a zone tap. */
private fun buildZoneActionCard(
    zone: InfestationZoneEntity,
    onNavigateToZoneDetail: (String) -> Unit,
    onNavigateToEditZone: (String) -> Unit,
    viewModel: MapViewModel
): MapActionCardData {
    return MapActionCardData(
        title = zone.name ?: "Zone",
        subtitle = statusLabel(zone.status),
        zoneId = zone.id,
        actions = listOf(
            MapCardAction("View Details", false, Icons.Default.Info) {
                onNavigateToZoneDetail(zone.id)
            },
            MapCardAction("Edit Zone", false, Icons.Default.Edit) {
                onNavigateToEditZone(zone.id)
            },
            MapCardAction("Delete", true, Icons.Default.Delete) {
                viewModel.deleteZone(zone.id)
            }
        )
    )
}

private fun statusLabel(status: String?): String = when (status) {
    "underTreatment" -> "Under Treatment"
    "cleared" -> "Cleared"
    else -> "Active"
}

/** Zone status -> semi-transparent fill color (ARGB int). */
private fun zoneStatusFillColor(status: String): Int = when (status) {
    "underTreatment" -> AndroidColor.argb(50, 255, 165, 0)   // orange
    "cleared" -> AndroidColor.argb(50, 0, 200, 0)            // green
    else -> AndroidColor.argb(50, 255, 0, 0)                 // red (active/new)
}

/** Zone status -> stroke color (ARGB int). */
private fun zoneStatusStrokeColor(status: String): Int = when (status) {
    "underTreatment" -> AndroidColor.argb(200, 255, 165, 0)
    "cleared" -> AndroidColor.argb(200, 0, 200, 0)
    else -> AndroidColor.argb(200, 255, 0, 0)
}

/**
 * Create a simple colored circle pin drawable for osmdroid Marker.
 * Produces a small filled circle with a white border.
 */
private fun createColoredPinDrawable(
    context: android.content.Context,
    color: Int
): android.graphics.drawable.Drawable {
    val size = (24 * context.resources.displayMetrics.density).toInt()
    val innerSize = (18 * context.resources.displayMetrics.density).toInt()

    val outerCircle = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setSize(size, size)
        setColor(AndroidColor.WHITE)
    }
    val innerCircle = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setSize(innerSize, innerSize)
        setColor(color)
    }
    return LayerDrawable(arrayOf(outerCircle, innerCircle)).apply {
        val inset = (size - innerSize) / 2
        setLayerInset(1, inset, inset, inset, inset)
        setBounds(0, 0, size, size)
    }
}

/**
 * Create a numbered circle pin for vertex markers in draw mode.
 * Yellow circle with the number drawn in black text.
 */
private fun createNumberedPinDrawable(
    context: android.content.Context,
    color: Int,
    number: Int
): android.graphics.drawable.Drawable {
    val density = context.resources.displayMetrics.density
    val size = (28 * density).toInt()

    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Draw circle
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 1, paint)

    // Draw border
    paint.apply {
        this.color = AndroidColor.BLACK
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 1, paint)

    // Draw number text
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.BLACK
        textSize = 12 * density
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(number.toString(), size / 2f, textY, textPaint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

/**
 * Haversine distance in meters between two lat/lon points.
 * Used for circle fallback radius calculation.
 */
private fun haversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
