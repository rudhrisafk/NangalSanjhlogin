// AdminReportsScreen.kt — NEW FILE

package com.nangalsanjh.app.ui.screens

import com.nangalsanjh.app.ui.bouncy.Button
import com.nangalsanjh.app.ui.bouncy.OutlinedButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.Report
import com.nangalsanjh.app.viewmodel.ReportViewModel

@Composable
fun AdminReportsScreen() {
    val viewModel: ReportViewModel = viewModel()
    val reports by viewModel.reports.collectAsState()
    val blockedUids by viewModel.blockedUids.collectAsState()
    var confirmBlockUid by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (reports.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("REPORTS  ·  ਰਿਪੋਰਟਾਂ", "User reports", "Review reports and block or unblock users.")
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reports yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ScreenHeader("REPORTS  ·  ਰਿਪੋਰਟਾਂ", "User reports", "Review reports and block or unblock users.", horizontalPadding = 0.dp) }
            items(reports) { report ->
                val isBlocked = report.reportedUid in blockedUids
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(report.reason, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                            if (report.status == "pending") {
                                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)) {
                                    Text("Pending", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Reported: ${report.reportedName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("By: ${report.reporterName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        if (report.details.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(report.details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isBlocked) {
                                OutlinedButton(onClick = { viewModel.unblockUser(report.reportedUid) }) { Text("Unblock") }
                            } else {
                                Button(
                                    onClick = { confirmBlockUid = report.reportedUid to report.reportedName },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) { Text("Block User") }
                            }
                            if (report.status == "pending") {
                                TextButton(onClick = { viewModel.resolveReport(report.id) }) { Text("Mark Reviewed") }
                            }
                        }
                    }
                }
            }
        }
    }

    confirmBlockUid?.let { (uid, name) ->
        AlertDialog(
            onDismissRequest = { confirmBlockUid = null },
            title = { Text("Block $name?") },
            text = { Text("They won't be able to use the app anymore.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.blockUser(uid); confirmBlockUid = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Block") }
            },
            dismissButton = { TextButton(onClick = { confirmBlockUid = null }) { Text(tr("Cancel")) } }
        )
    }
}