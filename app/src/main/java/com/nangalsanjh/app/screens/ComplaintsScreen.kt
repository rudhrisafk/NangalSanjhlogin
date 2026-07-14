package com.nangalsanjh.app.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.nangalsanjh.app.theme.*
import com.nangalsanjh.app.viewmodel.ComplaintModel
import com.nangalsanjh.app.viewmodel.ComplaintViewModel
import com.nangalsanjh.app.viewmodel.PointsViewModel

@Composable
fun ComplaintsScreen(userRole: String, userName: String = "") {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val pointsViewModel: PointsViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    val isLoading by complaintViewModel.isLoading.collectAsState()
    val submitSuccess by complaintViewModel.submitSuccess.collectAsState()
    var showComplaintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            showComplaintDialog = false
            complaintViewModel.resetSubmitSuccess()
            pointsViewModel.addPoints("complaint")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading && complaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (complaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NSEmptyState(
                    emoji = "📋",
                    title = "No complaints yet",
                    subtitle = "Be the first to report a civic issue",
                    actionText = if (userRole == "seeker") "Report Issue" else "",
                    onAction = if (userRole == "seeker") ({ showComplaintDialog = true }) else null
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(complaints) { complaint ->
                    RealComplaintCard(
                        complaint = complaint,
                        userRole = userRole,
                        onStatusUpdate = { id, status, update ->
                            complaintViewModel.updateComplaintStatus(id, status, update)
                        },
                        onDelete = { id -> complaintViewModel.deleteComplaint(id) },
                        onAddPoints = { uid, pts, reason ->
                            pointsViewModel.addPointsToUser(uid, pts, reason)
                        }
                    )
                }
            }
        }

        if (userRole == "seeker" || userRole.isEmpty()) {
            FloatingActionButton(
                onClick = { showComplaintDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.lg),
                shape = Radius.md,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showComplaintDialog) {
        NewComplaintDialog(
            onDismiss = { showComplaintDialog = false },
            onSubmit = { category, description, location, lat, lng ->
                complaintViewModel.submitComplaint(userName, category, description, location, lat, lng)
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun RealComplaintCard(
    complaint: ComplaintModel,
    userRole: String,
    onStatusUpdate: (String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onAddPoints: (String, Int, String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPointsDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("In Review") }
    var updateText by remember { mutableStateOf("") }
    var pointsToAdd by remember { mutableStateOf("") }
    var pointsReason by remember { mutableStateOf("") }
    val timeAgo = remember(complaint.timeStamp) { getTimeAgo(complaint.timeStamp) }

    val statusColor = when (complaint.status) {
        "Resolved" -> NangalColors.Green
        "In Review" -> NangalColors.Yellow
        else -> NangalColors.Red
    }

    val statusEmoji = when (complaint.status) {
        "Resolved" -> "✅"
        "In Review" -> "🔄"
        else -> "🔴"
    }

    NSCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    NSAvatar(
                        emoji = statusEmoji,
                        size = 40.dp,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            complaint.category,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    NSStatusBadge(text = complaint.status, color = statusColor)

                    if (userRole == "admin") {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mark In Review", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Text("🔄", fontSize = 14.sp) },
                                    onClick = { selectedStatus = "In Review"; showMenu = false; showUpdateDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mark Resolved", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Text("✅", fontSize = 14.sp) },
                                    onClick = { selectedStatus = "Resolved"; showMenu = false; showUpdateDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add Points", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) },
                                    leadingIcon = { Text("🏅", fontSize = 14.sp) },
                                    onClick = { showMenu = false; showPointsDialog = true }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                DropdownMenuItem(
                                    text = { Text("Delete", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) },
                                    onClick = { showMenu = false; showDeleteDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Reporter
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(complaint.reporter, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                complaint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(complaint.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (complaint.departmentUpdate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.md))
                NSDivider()
                Spacer(modifier = Modifier.height(Spacing.md))
                Surface(
                    shape = Radius.sm,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.AccountBalance, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            complaint.departmentUpdate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Update Dialog
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Update Status", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    NSStatusBadge(
                        text = "→ $selectedStatus",
                        color = if (selectedStatus == "Resolved") NangalColors.Green else NangalColors.Yellow
                    )
                    NSTextField(value = updateText, onValueChange = { updateText = it }, label = "Department Note", placeholder = "Add update for citizen...", singleLine = false, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                NSButton(
                    text = "Update",
                    onClick = { onStatusUpdate(complaint.id, selectedStatus, updateText); showUpdateDialog = false },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = { NSOutlinedButton(text = "Cancel", onClick = { showUpdateDialog = false }, modifier = Modifier.fillMaxWidth()) }
        )
    }

    // Points Dialog
    if (showPointsDialog) {
        AlertDialog(
            onDismissRequest = { showPointsDialog = false },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Add Seva Points", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text("Reporter: ${complaint.reporter}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Quick select
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        listOf("10", "25", "50", "100").forEach { pts ->
                            FilterChip(
                                selected = pointsToAdd == pts,
                                onClick = { pointsToAdd = pts },
                                label = { Text("+$pts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    NSTextField(value = pointsToAdd, onValueChange = { pointsToAdd = it }, label = "Custom Points", placeholder = "1–500", modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    NSTextField(value = pointsReason, onValueChange = { pointsReason = it }, label = "Reason", placeholder = "Why giving points?", modifier = Modifier.fillMaxWidth())

                    // Levels
                    NSCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text("Points Levels", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            listOf("🌱 Nagarik" to "0-99", "🥉 Seva Sevak" to "100-199", "🥈 Seva Mitra" to "200-299", "⭐ Seva Ratna" to "300-499", "🏆 Seva Samrat" to "500+").forEach { (level, range) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(level, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text(range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                NSButton(
                    text = "Add ${pointsToAdd.ifBlank { "0" }} Points",
                    onClick = {
                        val pts = pointsToAdd.toIntOrNull() ?: 0
                        if (pts > 0) onAddPoints(complaint.uid, pts.coerceAtMost(500), pointsReason.ifBlank { "Admin reward" })
                        showPointsDialog = false
                        pointsToAdd = ""
                        pointsReason = ""
                    },
                    enabled = pointsToAdd.toIntOrNull()?.let { it > 0 } == true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = { NSOutlinedButton(text = "Cancel", onClick = { showPointsDialog = false; pointsToAdd = ""; pointsReason = "" }, modifier = Modifier.fillMaxWidth()) }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = Radius.xl,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Complaint", style = MaterialTheme.typography.titleMedium) },
            text = { Text("This action cannot be undone.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { onDelete(complaint.id); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = Radius.md,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = { NSOutlinedButton(text = "Cancel", onClick = { showDeleteDialog = false }, modifier = Modifier.fillMaxWidth()) }
        )
    }
}

@Composable
fun NewComplaintDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Double, Double) -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("🕳️ Pothole") }
    var customCategory by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var locationFetched by remember { mutableStateOf(false) }
    val isOther = selectedCategory == "❓ Other"

    val categories = listOf("🕳️ Pothole", "💡 Street Light", "🗑️ Garbage", "💧 Water Leakage", "🚧 Road", "⚡ Electricity", "🏫 School", "🏥 Hospital", "🌳 Parks", "❓ Other")

    val locationPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { loc ->
                loc?.let { latitude = it.latitude; longitude = it.longitude; locationFetched = true }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radius.xl,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Report Issue", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                if (isOther) {
                    NSTextField(value = customCategory, onValueChange = { customCategory = it }, label = "Specify Issue", modifier = Modifier.fillMaxWidth())
                }

                NSTextField(value = description, onValueChange = { description = it }, label = "Describe the Issue", singleLine = false, minLines = 3, modifier = Modifier.fillMaxWidth())
                NSTextField(value = location, onValueChange = { location = it }, label = "Location / Area", leadingIcon = Icons.Default.LocationOn, modifier = Modifier.fillMaxWidth())

                // GPS Button
                Surface(
                    shape = Radius.md,
                    color = if (locationFetched) NangalColors.Green.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { loc ->
                                    loc?.let { latitude = it.latitude; longitude = it.longitude; locationFetched = true }
                                }
                            } else {
                                locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, null, tint = if (locationFetched) NangalColors.Green else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            if (locationFetched) "📍 Location Captured" else "Use GPS Location",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (locationFetched) NangalColors.Green else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            NSButton(
                text = "Submit",
                onClick = {
                    val cat = if (isOther && customCategory.isNotBlank()) "❓ $customCategory" else selectedCategory
                    onSubmit(cat, description, location, latitude, longitude)
                },
                enabled = description.isNotBlank() && location.isNotBlank() && (!isOther || customCategory.isNotBlank()),
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = { NSOutlinedButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth()) }
    )
}