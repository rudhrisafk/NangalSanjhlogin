package com.nangalsanjh.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    userName: String,
    onLogout: () -> Unit = {},
    onRoleSwitched: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAIAssistant by remember { mutableStateOf(false) }
    var showEmergency by remember { mutableStateOf(false) }
    var showBloodDonors by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showRoleSelection by remember { mutableStateOf(false) }
    var showAdminPasscodeDialog by remember { mutableStateOf(false) }   // NEW

    // ── ROLE SELECTION SCREEN (Switch Profile se yahan aata hai) ──
    if (showRoleSelection) {
        RoleSelectionScreen(
            onRoleSelected = { newRole ->
                if (newRole == "admin") {
                    // Admin select kiya -> seedha switch nahi, passcode dialog dikhao
                    showAdminPasscodeDialog = true
                } else {
                    showRoleSelection = false
                    showProfile = false
                    onRoleSwitched(newRole)
                }
            }
        )

        // Admin passcode dialog yahi pe overlay hoga (RoleSelectionScreen ke upar)
        if (showAdminPasscodeDialog) {
            AdminPasscodeInlineDialog(
                onDismiss = { showAdminPasscodeDialog = false },
                onVerified = {
                    showAdminPasscodeDialog = false
                    showRoleSelection = false
                    showProfile = false
                    onRoleSwitched("admin")
                }
            )
        }
        return
    }

    if (showProfile) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showProfile = false }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                ProfileScreen(
                    userName = userName,
                    userRole = userRole,
                    onLogout = {
                        showProfile = false
                        onLogout()
                    },
                    onSwitchProfile = {
                        showRoleSelection = true
                    }
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏙️ ", fontSize = 20.sp)
                        Text(
                            text = "Nangal Seva",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                        )
                    }
                },
                actions = {
                    if (userRole == "admin" || userRole == "worker") {
                        IconButton(onClick = { showEmergency = true }) {
                            Text("🚨", fontSize = 20.sp)
                        }
                    }
                    IconButton(onClick = { showProfile = true }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                getNavItems(userRole).forEachIndexed { index, item ->
                    if (item.isSOS) {
                        NavigationBarItem(
                            icon = {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFB71C1C),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text("🚨", fontSize = 18.sp)
                                            Text(
                                                "SOS",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            },
                            label = {},
                            selected = false,
                            onClick = { showEmergency = true }
                        )
                    } else {
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 11.sp) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (userRole != "admin") {
                FloatingActionButton(
                    onClick = { showAIAssistant = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Text("🤖", fontSize = 20.sp)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userRole) {
                "seeker" -> SeekerContent(selectedTab, userName, onLogout)
                "worker" -> WorkerContent(selectedTab, userName, onLogout)
                "admin" -> AdminContent(selectedTab, userName, onLogout)
                else -> SeekerContent(selectedTab, userName, onLogout)
            }
        }
    }

    if (showAIAssistant) {
        AIAssistantDialog { showAIAssistant = false }
    }

    if (showEmergency) {
        Dialog(
            onDismissRequest = {
                showEmergency = false
                showBloodDonors = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBloodDonors) {
                            IconButton(onClick = { showBloodDonors = false }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                        IconButton(onClick = {
                            showEmergency = false
                            showBloodDonors = false
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (showBloodDonors) {
                        BloodDonorsScreen()
                    } else {
                        EmergencyScreen(onBloodDonors = { showBloodDonors = true })
                    }
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Home,
    val isSOS: Boolean = false
)

fun getNavItems(role: String): List<NavItem> = when (role) {
    "worker" -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("My Jobs", Icons.Default.Work),
        NavItem("Map", Icons.Default.Map)
    )
    "admin" -> listOf(
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Resolved", Icons.Default.CheckCircle),
        NavItem("Map", Icons.Default.Map)
    )
    else -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("Services", Icons.Default.Build),
        NavItem("SOS", isSOS = true),
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Map", Icons.Default.Map)
    )
}

@Composable
fun SeekerContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "seeker")
        1 -> ServicesScreen()
        3 -> ComplaintsScreen(userRole = "seeker", userName = userName)
        4 -> MapScreen()
        else -> CommunityFeedScreen(userName = userName, userRole = "seeker")
    }
}

@Composable
fun WorkerContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName, userRole = "worker")
        1 -> WorkerJobsScreen()
        2 -> MapScreen()
        else -> CommunityFeedScreen(userName = userName, userRole = "worker")
    }
}

@Composable
fun AdminContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> ComplaintsScreen(userRole = "admin", userName = userName)
        1 -> ResolvedComplaintsScreen()
        2 -> MapScreen()
        else -> ComplaintsScreen(userRole = "admin", userName = userName)
    }
}