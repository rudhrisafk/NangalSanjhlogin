package com.nangalsanjh.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.ComplaintViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

const val NANGAL_LAT = 31.4983
const val NANGAL_LNG = 76.3762

@Composable
fun MapScreen() {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    val context = LocalContext.current

    var selectedComplaint by remember {
        mutableStateOf<com.nangalsanjh.app.viewmodel.ComplaintModel?>(null)
    }

    val pendingCount = complaints.count { it.status == "Pending" }
    val reviewCount = complaints.count { it.status == "In Review" }
    val resolvedCount = complaints.count { it.status == "Resolved" }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(NANGAL_LAT, NANGAL_LNG))

                    val centerMarker = Marker(this).apply {
                        position = GeoPoint(NANGAL_LAT, NANGAL_LNG)
                        title = "Nangal Town Center"
                        snippet = "ਨੰਗਲ ਸ਼ਹਿਰ"
                    }
                    overlays.add(centerMarker)
                }
            },
            update = { mapView ->
                if (mapView.overlays.size > 1) {
                    mapView.overlays.subList(1, mapView.overlays.size).clear()
                }
                complaints.forEach { complaint ->
                    if (complaint.latitude != 0.0 && complaint.longitude != 0.0) {
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(complaint.latitude, complaint.longitude)
                            title = complaint.category
                            snippet = "${complaint.status} — ${complaint.description}"
                            setOnMarkerClickListener { _, _ ->
                                selectedComplaint = complaint
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Legend Top Left
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    "🗺️ Nangal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LegendRow(color = Color.Red, label = "Pending ($pendingCount)")
                LegendRow(color = Color(0xFFFFB300), label = "In Review ($reviewCount)")
                LegendRow(color = Color(0xFF2E7D32), label = "Resolved ($resolvedCount)")
            }
        }

        // Total Top Right
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    complaints.size.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    "Total",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Empty State
        if (complaints.filter { it.latitude != 0.0 }.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No complaints on map yet",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "Submit a complaint with GPS location to see it here",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Selected Complaint
        selectedComplaint?.let { complaint ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            complaint.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val statusColor = when (complaint.status) {
                            "Resolved" -> Color(0xFF2E7D32)
                            "In Review" -> Color(0xFFFFB300)
                            else -> MaterialTheme.colorScheme.error
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                complaint.status,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp, vertical = 3.dp
                                ),
                                fontSize = 11.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "👤 ${complaint.reporter}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        complaint.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📍 ${complaint.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (complaint.departmentUpdate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Text(
                                "🏛️ ${complaint.departmentUpdate}",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectedComplaint = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = RoundedCornerShape(50.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}