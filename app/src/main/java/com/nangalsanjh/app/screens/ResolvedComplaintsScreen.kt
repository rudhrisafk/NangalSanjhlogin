package com.nangalsanjh.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.ComplaintViewModel

@Composable
fun ResolvedComplaintsScreen() {
    val complaintViewModel: ComplaintViewModel = viewModel()
    val complaints by complaintViewModel.complaints.collectAsState()
    
    val resolved = complaints.filter { it.status == "Resolved" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(resolved) { complaint ->
            RealComplaintCard(
                complaint = complaint, 
                userRole = "admin",
                onStatusUpdate = { id, status, update ->
                    complaintViewModel.updateComplaintStatus(id, status, update)
                }
            )
        }
    }
}
