package com.nangalsanjh.app.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BloodDonor(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val area: String = "",
    val uid: String = ""
)

class BloodDonorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _donors = MutableStateFlow<List<BloodDonor>>(emptyList())
    val donors: StateFlow<List<BloodDonor>> = _donors

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    init { fetchDonors() }

    fun fetchDonors() {
        _isLoading.value = true
        db.collection("blood_donors")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    BloodDonor(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        bloodGroup = doc.getString("bloodGroup") ?: "",
                        area = doc.getString("area") ?: "",
                        uid = doc.getString("uid") ?: ""
                    )
                } ?: emptyList()
                _donors.value = list
            }
    }

    fun registerAsDonor(
        name: String,
        phone: String,
        bloodGroup: String,
        area: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        val donor = hashMapOf(
            "name" to name,
            "phone" to phone,
            "bloodGroup" to bloodGroup,
            "area" to area,
            "uid" to uid,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("blood_donors")
            .add(donor)
            .addOnSuccessListener {
                _isLoading.value = false
                _registerSuccess.value = true
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun resetRegisterSuccess() {
        _registerSuccess.value = false
    }
}

@Composable
fun BloodDonorsScreen() {
    val viewModel: BloodDonorViewModel = viewModel()
    val donors by viewModel.donors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val context = LocalContext.current

    var selectedBloodGroup by remember { mutableStateOf("All") }
    var showRegisterDialog by remember { mutableStateOf(false) }

    val bloodGroups = listOf("All", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            showRegisterDialog = false
            viewModel.resetRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF880E4F))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "🩸 Blood Donors",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Nangal Blood Donor Directory",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = { showRegisterDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF880E4F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Register",
                        color = Color(0xFF880E4F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Blood Group Filter
        ScrollableTabRow(
            selectedTabIndex = bloodGroups.indexOf(selectedBloodGroup).coerceAtLeast(0),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF880E4F),
            edgePadding = 8.dp
        ) {
            bloodGroups.forEach { group ->
                Tab(
                    selected = selectedBloodGroup == group,
                    onClick = { selectedBloodGroup = group },
                    text = {
                        Text(
                            group,
                            fontSize = 13.sp,
                            fontWeight = if (selectedBloodGroup == group)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isLoading && donors.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF880E4F))
            }
        } else {
            val filtered = if (selectedBloodGroup == "All") donors
            else donors.filter { it.bloodGroup == selectedBloodGroup }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🩸", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No donors found",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF880E4F)
                            )
                        ) {
                            Text("Register as Donor")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { donor ->
                        BloodDonorCard(
                            donor = donor,
                            onCallClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${donor.phone}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        RegisterDonorDialog(
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, phone, bloodGroup, area ->
                viewModel.registerAsDonor(name, phone, bloodGroup, area)
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun BloodDonorCard(donor: BloodDonor, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF880E4F).copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        donor.bloodGroup,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF880E4F)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    donor.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "📍 ${donor.area}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onCallClick) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF880E4F)
                )
            }
        }
    }
}

@Composable
fun RegisterDonorDialog(
    onDismiss: () -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("A+") }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🩸 Register as Donor", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Mohalla") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Blood Group",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    bloodGroups.chunked(4).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { group ->
                                FilterChip(
                                    selected = selectedBloodGroup == group,
                                    onClick = { selectedBloodGroup = group },
                                    label = {
                                        Text(
                                            group,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF880E4F),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRegister(name, phone, selectedBloodGroup, area) },
                enabled = name.isNotBlank() && phone.isNotBlank() &&
                        area.isNotBlank() && !isLoading,
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
                    Text("Register")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}