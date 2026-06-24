package com.nangalsanjh.app.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    userName: String,
    onLogout: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAIAssistant by remember { mutableStateOf(false) }
    var showEmergency by remember { mutableStateOf(false) }

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
                    // 🚨 Emergency Button
                    IconButton(onClick = { showEmergency = true }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFB71C1C)
                        ) {
                            Text(
                                "🚨",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
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
        },
        floatingActionButton = {
            if (userRole != "admin") {
                FloatingActionButton(
                    onClick = { showAIAssistant = true },
                    containerColor = MaterialTheme.colorScheme.primary
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

    // AI Assistant
    if (showAIAssistant) {
        AIAssistantDialog { showAIAssistant = false }
    }

    // Emergency Screen
    if (showEmergency) {
        Dialog(
            onDismissRequest = { showEmergency = false },
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
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showEmergency = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    EmergencyScreen()
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

fun getNavItems(role: String): List<NavItem> = when (role) {
    "worker" -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("My Jobs", Icons.Default.Work),
        NavItem("Profile", Icons.Default.Person)
    )
    "admin" -> listOf(
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Resolved", Icons.Default.CheckCircle),
        NavItem("Profile", Icons.Default.Person)
    )
    else -> listOf(
        NavItem("Feed", Icons.Default.Home),
        NavItem("Services", Icons.Default.Build),
        NavItem("Complaints", Icons.Default.Report),
        NavItem("Profile", Icons.Default.Person)
    )
}

@Composable
fun SeekerContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName)
        1 -> ServicesScreen()
        2 -> ComplaintsScreen(userRole = "seeker", userName = userName)
        3 -> ProfileScreen(userName = userName, userRole = "seeker", onLogout = onLogout)
    }
}

@Composable
fun WorkerContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> CommunityFeedScreen(userName = userName)
        1 -> WorkerJobsScreen()
        2 -> ProfileScreen(userName = userName, userRole = "worker", onLogout = onLogout)
    }
}

@Composable
fun AdminContent(tab: Int, userName: String, onLogout: () -> Unit) {
    when (tab) {
        0 -> ComplaintsScreen(userRole = "admin", userName = userName)
        1 -> ResolvedComplaintsScreen()
        2 -> ProfileScreen(userName = userName, userRole = "admin", onLogout = onLogout)
    }
}