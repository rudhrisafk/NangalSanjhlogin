package com.nangalsanjh.app.ui.screens

import android.content.Intent
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
import androidx.core.net.toUri
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
    val uid: String = "",
)

class BloodDonorViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _donors = MutableStateFlow<List<BloodDonor>>(emptyList())
    val donors: StateFlow<List<BloodDonor>> = _donors

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

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

    fun registerAsDonor(name: String, phone: String, bloodGroup: String, area: String) {
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
            .addOnFailureListener { _isLoading.value = false }
    }

    fun updateDonor(
        donorId: String,
        name: String,
        phone: String,
        bloodGroup: String,
        area: String
    ) {
        _isLoading.value = true
        db.collection("blood_donors").document(donorId)
            .update(
                mapOf(
                    "name" to name,
                    "phone" to phone,
                    "bloodGroup" to bloodGroup,
                    "area" to area
                )
            )
            .addOnSuccessListener {
                _isLoading.value = false
                _updateSuccess.value = true
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    fun deleteDonor(donorId: String) {
        db.collection("blood_donors").document(donorId).delete()
    }

    fun resetRegisterSuccess() { _registerSuccess.value = false }
    fun resetUpdateSuccess() { _updateSuccess.value = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodDonorsScreen(userRole: String = "seeker") {
    val viewModel: BloodDonorViewModel = viewModel()
    val donors by viewModel.donors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val context = LocalContext.current

    var selectedBloodGroup by remember { mutableStateOf("All") }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var searchPhone by remember { mutableStateOf("") }
    var donorToEdit by remember { mutableStateOf<BloodDonor?>(null) }
    var donorToDelete by remember { mutableStateOf<BloodDonor?>(null) }
    var showBloodGroupDropdown by remember { mutableStateOf(false) }

    val bloodGroups = listOf("All", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val isAdmin = userRole == "admin"

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            showRegisterDialog = false
            viewModel.resetRegisterSuccess()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            donorToEdit = null
            viewModel.resetUpdateSuccess()
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
                        if (isAdmin) "Admin View — Full Control"
                        else "Nangal Blood Donor Directory",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Button(
                    onClick = { showRegisterDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF880E4F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Register", color = Color(0xFF880E4F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Admin Controls
        if (isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Phone Search
                OutlinedTextField(
                    value = searchPhone,
                    onValueChange = { searchPhone = it },
                    placeholder = { Text("Search by phone number...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF880E4F))
                    },
                    trailingIcon = {
                        if (searchPhone.isNotEmpty()) {
                            IconButton(onClick = { searchPhone = "" }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF880E4F),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                // Blood Group Dropdown Filter
                ExposedDropdownMenuBox(
                    expanded = showBloodGroupDropdown,
                    onExpandedChange = { showBloodGroupDropdown = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedBloodGroup == "All") "Filter by Blood Group" else "Blood Group: $selectedBloodGroup",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Text(
                                "🩸",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodGroupDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF880E4F),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showBloodGroupDropdown,
                        onDismissRequest = { showBloodGroupDropdown = false }
                    ) {
                        bloodGroups.forEach { group ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (group == "All") "All Blood Groups" else group,
                                            fontWeight = if (selectedBloodGroup == group)
                                                FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedBloodGroup == group)
                                                Color(0xFF880E4F)
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                        if (selectedBloodGroup == group) {
                                            Icon(
                                                Icons.Default.Check,
                                                null,
                                                tint = Color(0xFF880E4F),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedBloodGroup = group
                                    showBloodGroupDropdown = false
                                }
                            )
                        }
                    }
                }

                // Active filters info
                if ((searchPhone.isNotEmpty()) || (selectedBloodGroup != "All")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Active filters:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        if (searchPhone.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF880E4F).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "📞 $searchPhone",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    color = Color(0xFF880E4F)
                                )
                            }
                        }
                        if (selectedBloodGroup != "All") {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF880E4F).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "🩸 $selectedBloodGroup",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    color = Color(0xFF880E4F)
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                searchPhone = ""
                                selectedBloodGroup = "All"
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear all", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            // Non-admin blood group filter tabs
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
        }

        // Filtered donors list
        val filtered = donors
            .filter { donor ->
                if (searchPhone.isNotEmpty())
                    donor.phone.contains(searchPhone, ignoreCase = true)
                else true
            }
            .filter { donor ->
                if (selectedBloodGroup == "All") true
                else donor.bloodGroup == selectedBloodGroup
            }

        if (isLoading && donors.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF880E4F))
            }
        } else if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🩸", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (searchPhone.isNotEmpty())
                            "No donor found with phone: $searchPhone"
                        else if (selectedBloodGroup != "All")
                            "No $selectedBloodGroup donors registered yet"
                        else
                            "No donors found",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showRegisterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F))
                    ) {
                        Text("Register as Donor")
                    }
                }
            }
        } else {
            // Results count for admin
            if (isAdmin) {
                Text(
                    "${filtered.size} donor(s) found",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { donor ->
                    BloodDonorCard(
                        donor = donor,
                        isAdmin = isAdmin,
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${donor.phone}".toUri()
                            }
                            context.startActivity(intent)
                        },
                        onEditClick = { donorToEdit = donor },
                    ) { donorToDelete = donor }

                }
            }
        }
    }

    // Register Dialog
    if (showRegisterDialog) {
        RegisterDonorDialog(
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, phone, bloodGroup, area ->
                viewModel.registerAsDonor(name, phone, bloodGroup, area)
            },
            isLoading = isLoading
        )
    }

    // Admin Edit Dialog
    donorToEdit?.let { donor ->
        AdminEditDonorDialog(
            donor = donor,
            isLoading = isLoading,
            onDismiss = { donorToEdit = null },
            onSave = { name, phone, bloodGroup, area ->
                viewModel.updateDonor(donor.id, name, phone, bloodGroup, area)
            }
        )
    }

    // Delete Confirm Dialog
    donorToDelete?.let { donor ->
        AlertDialog(
            onDismissRequest = { donorToDelete = null },
            title = { Text("Delete Donor", fontWeight = FontWeight.Bold) },
            text = { Text("Remove ${donor.name} from blood donor list? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDonor(donor.id)
                        donorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { donorToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodDonorCard(
    donor: BloodDonor,
    isAdmin: Boolean,
    onCallClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blood Group Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF880E4F).copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        donor.bloodGroup,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "📞 ${donor.phone}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (donor.area.isNotEmpty()) {
                    Text(
                        "📍 ${donor.area}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Admin 3-dot menu
            if (isAdmin) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Call, null, tint = Color(0xFF880E4F), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call")
                                }
                            },
                            onClick = { showMenu = false; onCallClick() }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Details")
                                }
                            },
                            onClick = { showMenu = false; onEditClick() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Remove", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = { showMenu = false; onDeleteClick() }
                        )
                    }
                }
            } else {
                // Regular call button
                IconButton(onClick = onCallClick) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF880E4F))
                }
            }
        }
    }
}

// Admin Edit Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditDonorDialog(
    donor: BloodDonor,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var name by remember { mutableStateOf(donor.name) }
    var phone by remember { mutableStateOf(donor.phone) }
    var area by remember { mutableStateOf(donor.area) }
    var selectedBloodGroup by remember { mutableStateOf(donor.bloodGroup) }
    var showDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🩸", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Donor — Admin", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Mohalla") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Blood Group Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDropdown,
                    onExpandedChange = { showDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedBloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Group") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF880E4F)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        bloodGroups.forEach { group ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        group,
                                        fontWeight = if (selectedBloodGroup == group)
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedBloodGroup == group)
                                            Color(0xFF880E4F)
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedBloodGroup = group
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), phone.trim(), selectedBloodGroup, area.trim()) },
                enabled = name.isNotBlank() && phone.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Register Donor Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDonorDialog(
    onDismiss: () -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    isLoading: Boolean
) {
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("A+") }
    var showDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🩸 Register as Donor", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Mohalla") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = showDropdown,
                    onExpandedChange = { showDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedBloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Group") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF880E4F)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        bloodGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedBloodGroup = group
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRegister(name, phone, selectedBloodGroup, area) },
                enabled = name.isNotBlank() && phone.isNotBlank() &&
                        area.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
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