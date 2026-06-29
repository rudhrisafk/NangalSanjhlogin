package com.nangalsanjh.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    userName: String,
    userRole: String,
    onLogout: () -> Unit = {}
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val updateSuccess by profileViewModel.updateSuccess.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showBloodGroupDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            showEditDialog = false
            showCategoryDialog = false
            showBloodGroupDialog = false
            profileViewModel.resetUpdateSuccess()
        }
    }

    val roleEmoji = when (profile.role) {
        "worker" -> "👷"
        "admin" -> "🏛️"
        else -> "👤"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(roleEmoji, fontSize = 48.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && profile.name.isEmpty()) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Text(
                profile.name.ifEmpty { userName },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                profile.phone,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    profile.role.replaceFirstChar { it.uppercase() }.ifEmpty { userRole },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (profile.role == "worker" && profile.category.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        profile.category,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (profile.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        profile.address,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Blood Group Badge
            if (profile.bloodGroup.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF880E4F).copy(alpha = 0.15f)
                ) {
                    Text(
                        "🩸 ${profile.bloodGroup}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF880E4F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(count = profile.postsCount.toString(), label = "Posts", emoji = "📝")
                Divider(
                    modifier = Modifier.height(50.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                StatCard(
                    count = profile.complaintsCount.toString(),
                    label = "Complaints",
                    emoji = "📋"
                )
                if (profile.role == "worker") {
                    Divider(
                        modifier = Modifier.height(50.dp).width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    StatCard(
                        count = profile.jobsDone.toString(),
                        label = "Jobs Done",
                        emoji = "✅"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Items
        ProfileMenuItem(
            icon = Icons.Default.Edit,
            title = "Edit Profile",
            subtitle = "Change name or address",
            onClick = { showEditDialog = true }
        )

        // Worker — Change Category
        if (profile.role == "worker") {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(
                icon = Icons.Default.Build,
                title = "Change Work Category",
                subtitle = profile.category.ifEmpty { "Set your category" },
                onClick = { showCategoryDialog = true }
            )
        }

        // Blood Group Change
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Favorite,
            title = "Blood Group",
            subtitle = if (profile.bloodGroup.isEmpty()) "Set your blood group"
            else "Current: ${profile.bloodGroup}",
            onClick = { showBloodGroupDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.History,
            title = "My Posts",
            subtitle = "${profile.postsCount} posts",
            onClick = {}
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Report,
            title = "My Complaints",
            subtitle = "${profile.complaintsCount} complaints",
            onClick = {}
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Info,
            title = "About Nangal Seva",
            subtitle = "Version 1.0",
            onClick = {}
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            subtitle = "Sign out from app",
            onClick = onLogout,
            isDestructive = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentName = profile.name,
            currentAddress = profile.address,
            isLoading = isLoading,
            onDismiss = { showEditDialog = false },
            onSave = { name, address ->
                profileViewModel.updateProfile(name, address)
            }
        )
    }

    // Change Category Dialog
    if (showCategoryDialog) {
        ChangeCategoryDialog(
            currentCategory = profile.category,
            isLoading = isLoading,
            onDismiss = { showCategoryDialog = false },
            onSave = { newCategory ->
                profileViewModel.updateCategory(newCategory)
            }
        )
    }

    // Blood Group Dialog
    if (showBloodGroupDialog) {
        ChangeBloodGroupDialog(
            currentBloodGroup = profile.bloodGroup,
            isLoading = isLoading,
            onDismiss = { showBloodGroupDialog = false },
            onSave = { bloodGroup, isDonor ->
                profileViewModel.updateBloodGroup(bloodGroup, isDonor)
            }
        )
    }
}

@Composable
fun StatCard(count: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            count,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = if (isDestructive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAddress: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var address by remember { mutableStateOf(currentAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Area / Mohalla") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), address.trim()) },
                enabled = name.isNotBlank() && address.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChangeCategoryDialog(
    currentCategory: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val categories = listOf(
        "🔧 Plumber", "⚡ Electrician", "🎨 Painter",
        "🪚 Carpenter", "🧱 Mason", "🧹 Cleaner", "👷 Labour", "❓ Other"
    )
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    var customCategory by remember { mutableStateOf("") }
    val isOther = selectedCategory == "❓ Other"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Work Category", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            category,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (isOther) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Enter your work type") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val final = if (isOther && customCategory.isNotBlank())
                        customCategory.trim() else selectedCategory
                    onSave(final)
                },
                enabled = selectedCategory.isNotEmpty() &&
                        (!isOther || customCategory.isNotBlank()) && !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ChangeBloodGroupDialog(
    currentBloodGroup: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var selectedBloodGroup by remember { mutableStateOf(currentBloodGroup) }
    var isDonor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🩸 Blood Group", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Select your blood group",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    bloodGroups.chunked(4).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { group ->
                                FilterChip(
                                    selected = selectedBloodGroup == group,
                                    onClick = {
                                        selectedBloodGroup =
                                            if (selectedBloodGroup == group) "" else group
                                    },
                                    label = {
                                        Text(
                                            group,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF880E4F),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                if (selectedBloodGroup.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDonor,
                            onCheckedChange = { isDonor = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF880E4F)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "I am willing to donate blood if needed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedBloodGroup, isDonor) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF880E4F)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}