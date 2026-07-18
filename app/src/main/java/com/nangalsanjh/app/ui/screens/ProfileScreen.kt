package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nangalsanjh.app.viewmodel.ComplaintViewModel
import com.nangalsanjh.app.viewmodel.PointsViewModel
import com.nangalsanjh.app.viewmodel.PostViewModel
import com.nangalsanjh.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    userName: String,
    userRole: String,
    onLogout: () -> Unit = {},
    onRoleSwitch: ((String) -> Unit)? = null,
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val pointsViewModel: PointsViewModel = viewModel()
    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val updateSuccess by profileViewModel.updateSuccess.collectAsState()
    val userPoints by pointsViewModel.userPoints.collectAsState()

    var showEditDialog by remember { mutableStateOf(value = false) }
    var showCategoryDialog by remember { mutableStateOf(value = false) }
    var showBloodGroupDialog by remember { mutableStateOf(value = false) }
    var showMyPosts by remember { mutableStateOf(value = false) }
    var showMyComplaints by remember { mutableStateOf(value = false) }
    var showAbout by remember { mutableStateOf(value = false) }
    var showSwitchProfile by remember { mutableStateOf(value = false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            showEditDialog = false
            showCategoryDialog = false
            showBloodGroupDialog = false
            profileViewModel.resetUpdateSuccess()
        }
    }

    val currentRole = userRole

    val roleEmoji = when (currentRole) {
        "worker" -> "👷"
        "admin" -> "🏛️"
        else -> "👤"
    }

    if (showMyPosts) {
        MyPostsScreen { showMyPosts = false }
        return
    }

    if (showMyComplaints) {
        MyComplaintsScreen { showMyComplaints = false }
        return
    }

    if (showAbout) {
        AboutScreen { showAbout = false }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        currentRole.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (profile.bloodGroup.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF880E4F).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "🩸 ${profile.bloodGroup}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF880E4F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (profile.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Home, null,
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (currentRole == "seeker") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                userPoints.level,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${userPoints.points} Seva Points",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                userPoints.points.toString(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = (userPoints.points.toFloat() /
                            userPoints.nextLevelPoints.toFloat()).coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "${userPoints.points}/${userPoints.nextLevelPoints} pts to next level",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "🏆 Seva Points Levels",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            listOf(
                                "🌱 Nagarik" to "0 - 99",
                                "🥉 Seva Sevak" to "100 - 199",
                                "🥈 Seva Mitra" to "200 - 299",
                                "⭐ Seva Ratna" to "300 - 499",
                                "🏆 Seva Samrat" to "500+"
                            ).forEach { (level, range) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        level,
                                        fontSize = 12.sp,
                                        color = if (userPoints.level == level)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontWeight = if (userPoints.level == level)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        range,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(count = profile.postsCount.toString(), label = "Posts", emoji = "📝")
                HorizontalDivider(
                    modifier = Modifier.height(50.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                StatCard(
                    count = profile.complaintsCount.toString(),
                    label = "Complaints",
                    emoji = "📋"
                )
                if (currentRole == "worker") {
                    HorizontalDivider(
                        modifier = Modifier.height(50.dp).width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    StatCard(count = profile.jobsDone.toString(), label = "Jobs", emoji = "✅")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuItem(
            icon = Icons.Default.Edit,
            title = "Edit Profile",
            subtitle = "Change name or address",
            onClick = { showEditDialog = true }
        )

        if (currentRole == "worker") {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(
                icon = Icons.Default.Build,
                title = "Change Work Category",
                subtitle = profile.category.ifEmpty { "Set your category" },
                onClick = { showCategoryDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Favorite,
            title = "Blood Group",
            subtitle = if (profile.bloodGroup.isEmpty()) "Set your blood group"
            else "Current: ${profile.bloodGroup}",
            onClick = { showBloodGroupDialog = true }
        )

        if (currentRole == "worker") {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.Article,
                title = "My Posts",
                subtitle = "${profile.postsCount} posts",
                onClick = { showMyPosts = true }
            )
        }

        if (currentRole == "seeker") {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileMenuItem(
                icon = Icons.Default.Report,
                title = "My Complaints",
                subtitle = "${profile.complaintsCount} complaints",
                onClick = { showMyComplaints = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.Info,
            title = "About Nangal Seva",
            subtitle = "Version 1.0 • Nangal, Punjab",
            onClick = { showAbout = true }
        )

        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.Default.SwitchAccount,
            title = "Switch Profile",
            subtitle = "Currently: ${currentRole.replaceFirstChar { it.uppercase() }}",
            onClick = { showSwitchProfile = true }
        )

        Spacer(modifier = Modifier.height(8.dp))
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = "Logout",
            subtitle = "Sign out from app",
            onClick = onLogout,
            isDestructive = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = profile.name,
            currentAddress = profile.address,
            isLoading = isLoading,
            onDismiss = { showEditDialog = false },
            onSave = { name, address -> profileViewModel.updateProfile(name, address) }
        )
    }

    if (showCategoryDialog) {
        ChangeCategoryDialog(
            currentCategory = profile.category,
            isLoading = isLoading,
            onDismiss = { showCategoryDialog = false },
            onSave = { newCategory -> profileViewModel.updateCategory(newCategory) }
        )
    }

    if (showBloodGroupDialog) {
        ChangeBloodGroupDialog(
            currentBloodGroup = profile.bloodGroup,
            currentIsDonor = profile.isDonor,
            isLoading = isLoading,
            onDismiss = { showBloodGroupDialog = false },
            onSave = { bloodGroup, isDonor ->
                profileViewModel.updateBloodGroup(bloodGroup, isDonor)
            }
        )
    }

    if (showSwitchProfile) {
        SwitchProfileDialog(
            currentRole = currentRole,
            onDismiss = { showSwitchProfile = false },
            onSwitch = { newRole ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@SwitchProfileDialog
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("role", newRole)
                    .addOnSuccessListener {
                        showSwitchProfile = false
                        onRoleSwitch?.invoke(newRole)
                    }
            }
        )
    }
}

// ── Switch Profile Dialog ────────────────────────────────────
@Composable
fun SwitchProfileDialog(
    currentRole: String,
    onDismiss: () -> Unit,
    onSwitch: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var showAdminPasscode by remember { mutableStateOf(false) }
    var passcode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    if (showAdminPasscode) {
        AlertDialog(
            onDismissRequest = {
                showAdminPasscode = false
                passcode = ""
                passcodeError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Passcode", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "Enter department passcode to switch to Admin role",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = {
                            passcode = it
                            passcodeError = null
                        },
                        label = { Text("Passcode") },
                        singleLine = true,
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = passcodeError != null
                    )
                    if (passcodeError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            passcodeError!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passcode == ADMIN_PASSCODE) {
                            onSwitch("admin")
                            showAdminPasscode = false
                            passcode = ""
                        } else {
                            passcodeError = "⚠️ Invalid passcode"
                            passcode = ""
                        }
                    },
                    enabled = passcode.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Verify & Switch") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminPasscode = false
                        passcode = ""
                        passcodeError = null
                    }
                ) { Text("Cancel") }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SwitchAccount, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Profile", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Select your role. Admin requires a passcode.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    Triple("seeker", "👤", "Find workers & report issues"),
                    Triple("worker", "👷", "Offer your services"),
                    Triple("admin", "🏛️", "Municipal Official (passcode required)")
                ).forEach { (role, emoji, desc) ->
                    val isSelected = selectedRole == role
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            BorderStroke(
                                2.dp, MaterialTheme.colorScheme.primary
                            )
                        else null,
                        onClick = { selectedRole = role }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    role.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (role == "admin") {
                                Icon(
                                    Icons.Default.Lock, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRole == currentRole) {
                        onDismiss()
                        return@Button
                    }
                    if (selectedRole == "admin") {
                        showAdminPasscode = true
                    } else {
                        onSwitch(selectedRole)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (selectedRole == "admin") "Continue →"
                    else "Switch to ${selectedRole.replaceFirstChar { it.uppercase() }}"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── My Posts Screen ──────────────────────────────────────────
@Composable
fun MyPostsScreen(onBack: () -> Unit) {
    val postViewModel: PostViewModel = viewModel()
    val allPosts by postViewModel.posts.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val myPosts = allPosts.filter { it.uid == currentUid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "My Posts", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (myPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No posts yet", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Post your services to get started", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myPosts) { post ->
                    RealPostCard(post = post, userRole = "worker", onContactClick = {})
                }
            }
        }
    }
}

// ── My Complaints Screen ─────────────────────────────────────
@Composable
fun MyComplaintsScreen(onBack: () -> Unit) {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val allComplaints by complaintViewModel.complaints.collectAsState()
    val isLoading by complaintViewModel.isLoading.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val myComplaints = allComplaints.filter { it.uid == currentUid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "My Complaints", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (myComplaints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No complaints yet", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("Report a civic issue to get started", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myComplaints) { complaint ->
                    RealComplaintCard(
                        complaint = complaint,
                        userRole = "seeker",
                        onStatusUpdate = { _, _, _ -> },
                        onDelete = {},
                        onAddPoints = { _, _, _ -> }
                    )
                }
            }
        }
    }
}

// ── About Screen ─────────────────────────────────────────────
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "About Nangal Seva", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏙️", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nangal Seva", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("ਨੰਗਲ ਸੇਵਾ", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Apna Shehar, Apni Zimmedari", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(32.dp))
            AboutCard("🎯", "Our Mission", "Nangal Seva is a hyperlocal community platform built for the town of Nangal, Punjab. Our goal is to connect citizens, local workers, and municipal departments in one powerful mobile application.")
            Spacer(modifier = Modifier.height(16.dp))
            AboutCard("👥", "Who We Serve", "• Citizens (Seekers) — Report civic issues, find local workers, earn Seva Points\n• Workers — Showcase services, connect with people in need\n• Admin (Municipal Officials) — Manage complaints, update statuses, reward active citizens")
            Spacer(modifier = Modifier.height(16.dp))
            AboutCard("✨", "Key Features", "• Community Feed — Local service posts\n• Complaint Portal — Civic issue reporting with GPS\n• Services Directory — Verified local workers\n• Blood Donors — Emergency blood contact\n• Live Map — Complaint tracking on Nangal map\n• AI Assistant — Powered by Gemini\n• Emergency SOS — One tap emergency contacts\n• Seva Points — Reward system for active citizens")
            Spacer(modifier = Modifier.height(16.dp))
            AboutCard("📍", "Location", "Nangal, District Rupnagar, Punjab, India\nNear Bhakra Nangal Dam")
            Spacer(modifier = Modifier.height(16.dp))
            AboutCard("ℹ️", "App Info", "Version: 1.0\nPlatform: Android\nBuilt with: Kotlin + Jetpack Compose\nBackend: Firebase Firestore\nAI: Google Gemini")
            Spacer(modifier = Modifier.height(24.dp))
            Text("Made with ❤️ for Nangal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun AboutCard(emoji: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 20.sp)
        }
    }
}

@Composable
fun StatCard(count: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, contentDescription = title,
                        tint = if (isDestructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EditProfileDialog(currentName: String, currentAddress: String, isLoading: Boolean, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    var address by remember { mutableStateOf(currentAddress) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Area / Mohalla") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim(), address.trim()) }, enabled = name.isNotBlank() && address.isNotBlank() && !isLoading, shape = RoundedCornerShape(8.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary) else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ChangeCategoryDialog(currentCategory: String, isLoading: Boolean, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val categories = listOf("🔧 Plumber", "⚡ Electrician", "🎨 Painter", "🪚 Carpenter", "🧱 Mason", "🧹 Cleaner", "👷 Labour", "❓ Other")
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    var customCategory by remember { mutableStateOf("") }
    val isOther = selectedCategory == "❓ Other"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Work Category", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                categories.forEach { category ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedCategory == category, onClick = { selectedCategory = category }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                        Text(category, fontSize = 14.sp)
                    }
                }
                if (isOther) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = customCategory, onValueChange = { customCategory = it }, label = { Text("Enter your work type") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { val final = if (isOther && customCategory.isNotBlank()) customCategory.trim() else selectedCategory; onSave(final) }, enabled = selectedCategory.isNotEmpty() && (!isOther || customCategory.isNotBlank()) && !isLoading, shape = RoundedCornerShape(8.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary) else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ChangeBloodGroupDialog(
    currentBloodGroup: String,
    currentIsDonor: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var selectedBloodGroup by remember { mutableStateOf(currentBloodGroup) }
    var isDonor by remember { mutableStateOf(currentIsDonor) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🩸 Blood Group", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                bloodGroups.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { group ->
                            FilterChip(selected = selectedBloodGroup == group, onClick = { selectedBloodGroup = if (selectedBloodGroup == group) "" else group }, label = { Text(group, fontSize = 12.sp, fontWeight = FontWeight.Bold) }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF880E4F), selectedLabelColor = Color.White))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                if (selectedBloodGroup.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isDonor, onCheckedChange = { isDonor = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF880E4F)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I am willing to donate blood if needed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedBloodGroup, isDonor) }, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)), shape = RoundedCornerShape(8.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White) else Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}