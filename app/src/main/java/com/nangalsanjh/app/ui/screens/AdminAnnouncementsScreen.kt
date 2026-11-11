// AdminAnnouncementsScreen.kt — FULL FILE (removed size slider + span selector)

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nangalsanjh.app.viewmodel.Announcement
import com.nangalsanjh.app.viewmodel.AnnouncementViewModel

@Composable
fun AdminAnnouncementsScreen() {
    val viewModel: AnnouncementViewModel = viewModel()
    val announcements by viewModel.announcements.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var addAsHeadline by remember { mutableStateOf(false) }
    var editingAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var deletingAnnouncement by remember { mutableStateOf<Announcement?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
    OutlinedButton(
        onClick = { addAsHeadline = true; showAddDialog = true },
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Headline (top news ticker)")
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (announcements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📰", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No announcements yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(announcements) { announcement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (announcement.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = announcement.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when (announcement.type) {
                                        "emergency" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        "ad" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        announcement.type.replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        fontSize = 11.sp,
                                        color = when (announcement.type) {
                                            "emergency" -> MaterialTheme.colorScheme.error
                                            "ad" -> Color(0xFF2E7D32)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editingAnnouncement = announcement }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                    }
                                    Switch(
                                        checked = announcement.isActive,
                                        onCheckedChange = { viewModel.toggleActive(announcement.id, it) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(announcement.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            if (announcement.bloodType.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("🩸 ${announcement.bloodType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                            if (announcement.contact.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📞 ${announcement.contact}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                TextButton(onClick = { editingAnnouncement = announcement }) { Text("Edit") }
                                TextButton(onClick = { deletingAnnouncement = announcement }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { addAsHeadline = false; showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "New announcement") }
    }
    }

    if (showAddDialog) {
        AnnouncementFormDialog(
            existing = null,
            initialType = if (addAsHeadline) "headline" else "news",
            onDismiss = { showAddDialog = false },
            onSave = { type, title, desc, contact, bloodType, location, imageUrl ->
                viewModel.createAnnouncement(type, title, desc, contact, bloodType, location, imageUrl)
                showAddDialog = false
            }
        )
    }

    editingAnnouncement?.let { existing ->
        AnnouncementFormDialog(
            existing = existing,
            onDismiss = { editingAnnouncement = null },
            onSave = { type, title, desc, contact, bloodType, location, imageUrl ->
                viewModel.updateAnnouncement(existing.id, type, title, desc, contact, bloodType, location, imageUrl)
                editingAnnouncement = null
            }
        )
    }

    deletingAnnouncement?.let { announcement ->
        AlertDialog(
            onDismissRequest = { deletingAnnouncement = null },
            title = { Text("Delete this announcement?", fontWeight = FontWeight.Bold) },
            text = { Text("This can't be undone.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAnnouncement(announcement.id); deletingAnnouncement = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingAnnouncement = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AnnouncementFormDialog(
    existing: Announcement?,
    initialType: String = "news",
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var type by remember { mutableStateOf(existing?.type ?: initialType) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var contact by remember { mutableStateOf(existing?.contact ?: "") }
    var bloodType by remember { mutableStateOf(existing?.bloodType ?: "") }
    var location by remember { mutableStateOf(existing?.location ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }

    val types = listOf("headline" to "Headline (ticker)", "news" to "📰 News", "ad" to "📢 Ad/Promotion", "emergency" to "🚨 Emergency Need")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Announcement" else "Edit Announcement", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(types) { (value, label) ->
                        FilterChip(
                            selected = type == value,
                            onClick = { type = value },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(if (type == "headline") "Headline text" else "Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (type != "headline") {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (optional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                }

                if (type == "emergency") {
                    Text("Blood Type (optional)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(bloodGroups) { bg ->
                            FilterChip(
                                selected = bloodType == bg,
                                onClick = { bloodType = if (bloodType == bg) "" else bg },
                                label = { Text(bg, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                if (type != "headline") OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(type, title.trim(), if (type == "headline") title.trim() else description.trim(), contact.trim(), bloodType, location.trim(), imageUrl.trim()) },
                enabled = title.isNotBlank() && (type == "headline" || description.isNotBlank())
            ) { Text(if (existing == null) "Publish" else "Save Changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}