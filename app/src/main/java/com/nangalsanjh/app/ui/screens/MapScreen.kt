// MapScreen.kt — FULL FILE (admin: all complaints. seeker: only own complaints + own active job. worker: ONLY active job's seeker location, nothing else)

package com.nangalsanjh.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.nangalsanjh.app.viewmodel.ComplaintModel
import com.nangalsanjh.app.viewmodel.ComplaintViewModel
import com.nangalsanjh.app.viewmodel.Post
import com.nangalsanjh.app.viewmodel.PostViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

const val NANGAL_LAT = 31.4983
const val NANGAL_LNG = 76.3762

@Composable
fun MapScreen(userRole: String = "seeker") {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val allComplaints by complaintViewModel.complaints.collectAsState()
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedComplaint by remember { mutableStateOf<ComplaintModel?>(null) }

    // NEW: role-scoped complaint visibility
    // admin -> everything. seeker -> only their own. worker -> none, ever.
    val visibleComplaints = remember(allComplaints, userRole, uid) {
        when (userRole) {
            "admin" -> allComplaints
            "seeker" -> allComplaints.filter { it.uid == uid }
            else -> emptyList() // worker sees no complaints on map
        }
    }

    // Active assigned job: worker sees the job they accepted, seeker sees their own accepted request
    val activeJob = remember(posts, userRole, uid) {
        posts.firstOrNull {
            it.status == "assigned" &&
                    (if (userRole == "worker") it.assignedWorkerUid == uid else it.uid == uid)
        }
    }

    var myLocation by remember { mutableStateOf<GeoPoint?>(null) }

    DisposableEffect(activeJob?.id, userRole) {
        val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        var listener: LocationListener? = null

        if (userRole == "worker" && activeJob != null && hasPermission) {
            listener = LocationListener { loc: Location ->
                myLocation = GeoPoint(loc.latitude, loc.longitude)
            }
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 5f, listener)
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                    myLocation = GeoPoint(it.latitude, it.longitude)
                }
            } catch (ex: SecurityException) { }
        }

        onDispose {
            listener?.let { lm.removeUpdates(it) }
        }
    }

    val pendingCount = visibleComplaints.count { it.status == "Pending" }
    val reviewCount = visibleComplaints.count { it.status == "In Review" }
    val resolvedCount = visibleComplaints.count { it.status == "Resolved" }

    // Worker with no active job: nothing to show at all — just a blank town map
    val showBlankWorkerMap = userRole == "worker" && activeJob == null

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
                mapView.overlays.clear()

                val centerMarker = Marker(mapView).apply {
                    position = GeoPoint(NANGAL_LAT, NANGAL_LNG)
                    title = "Nangal Town Center"
                }
                mapView.overlays.add(centerMarker)

                if (userRole == "worker") {
                    // Worker map: ONLY the active job's seeker location. Nothing else, ever.
                    if (activeJob != null) {
                        val jobPoint = GeoPoint(activeJob.latitude, activeJob.longitude)
                        val jobMarker = Marker(mapView).apply {
                            position = jobPoint
                            title = "🎯 ${activeJob.userName}'s Location"
                            snippet = activeJob.address
                        }
                        mapView.overlays.add(jobMarker)

                        if (myLocation != null) {
                            val myMarker = Marker(mapView).apply {
                                position = myLocation
                                title = "You"
                            }
                            mapView.overlays.add(myMarker)

                            val line = Polyline().apply {
                                setPoints(listOf(myLocation, jobPoint))
                                outlinePaint.color = android.graphics.Color.parseColor("#1565C0")
                                outlinePaint.strokeWidth = 6f
                            }
                            mapView.overlays.add(line)

                            val midLat = (myLocation!!.latitude + jobPoint.latitude) / 2
                            val midLng = (myLocation!!.longitude + jobPoint.longitude) / 2
                            mapView.controller.setCenter(GeoPoint(midLat, midLng))
                            mapView.controller.setZoom(15.0)
                        } else {
                            mapView.controller.setCenter(jobPoint)
                            mapView.controller.setZoom(16.0)
                        }
                    }
                    // else: blank map, no markers at all besides town center
                } else if (activeJob != null) {
                    // Seeker with an active accepted job: show that tracking view
                    val jobPoint = GeoPoint(activeJob.latitude, activeJob.longitude)
                    val jobMarker = Marker(mapView).apply {
                        position = jobPoint
                        title = "🎯 Your Requested Location"
                        snippet = activeJob.address
                    }
                    mapView.overlays.add(jobMarker)
                    mapView.controller.setCenter(jobPoint)
                    mapView.controller.setZoom(16.0)
                } else {
                    // Seeker (no active job) or Admin: show scoped complaints
                    visibleComplaints.forEach { complaint ->
                        if (complaint.latitude != 0.0 && complaint.longitude != 0.0) {
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(complaint.latitude, complaint.longitude)
                                title = complaint.category
                                snippet = "${complaint.status} — ${complaint.description}"
                                setOnMarkerClickListener { _, _ -> selectedComplaint = complaint; true }
                            }
                            mapView.overlays.add(marker)
                        }
                    }
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (userRole == "worker") {
            if (activeJob != null) {
                // Active job info card — bottom
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("On the way to customer", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("👤 ${activeJob.userName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        if (activeJob.address.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📍 ${activeJob.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${activeJob.userPhone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = { postViewModel.completeJob(activeJob.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Mark Complete", fontSize = 13.sp) }
                        }
                    }
                }
            } else {
                // Blank state for worker — no active job
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗺️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No active job right now",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            "Accept a job to see the customer's exact location here",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (activeJob != null) {
            // Seeker with active job — tracking card
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Worker on the way", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("👷 ${activeJob.assignedWorkerName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:${activeJob.userPhone}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Worker", fontSize = 13.sp)
                    }
                }
            }
        } else {
            // Admin or seeker (no active job): scoped complaint legend + empty state
            Card(
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        if (userRole == "admin") "🗺️ Nangal" else "🗺️ Your Complaints",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LegendRow(color = Color.Red, label = "Pending ($pendingCount)")
                    LegendRow(color = Color(0xFFFFB300), label = "In Review ($reviewCount)")
                    LegendRow(color = Color(0xFF2E7D32), label = "Resolved ($resolvedCount)")
                }
            }

            if (userRole == "admin") {
                Card(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(visibleComplaints.size.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                        Text("Total", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            if (visibleComplaints.filter { it.latitude != 0.0 }.isEmpty()) {
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (userRole == "admin") "No complaints on map yet" else "You haven't filed any complaints yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (userRole != "admin") {
                            Text(
                                "Submit a complaint with GPS location to see it here",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        selectedComplaint?.let { complaint ->
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(complaint.category, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        val statusColor = when (complaint.status) {
                            "Resolved" -> Color(0xFF2E7D32)
                            "In Review" -> Color(0xFFFFB300)
                            else -> MaterialTheme.colorScheme.error
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.15f)) {
                            Text(
                                complaint.status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("👤 ${complaint.reporter}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(complaint.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("📍 ${complaint.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    if (complaint.departmentUpdate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                            Text(
                                "🏛️ ${complaint.departmentUpdate}",
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { selectedComplaint = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Surface(modifier = Modifier.size(10.dp), shape = RoundedCornerShape(50.dp), color = color) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}