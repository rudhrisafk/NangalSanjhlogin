// ReportUserDialog.kt — NEW FILE (reusable report component)

package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.ReportViewModel

@Composable
fun ReportUserDialog(
    reportedUid: String,
    reportedName: String,
    reporterName: String,
    onDismiss: () -> Unit
) {
    val reportViewModel: ReportViewModel = viewModel()
    var reason by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val reasons = listOf("Abusive behavior", "Fraud / Scam", "Didn't show up", "Fake profile", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (submitted) "Reported" else "Report $reportedName") },
        text = {
            if (submitted) {
                Text("Thanks, admin will review this.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reasons.forEach { r ->
                        Row {
                            RadioButton(selected = reason == r, onClick = { reason = r })
                            Text(r, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("More details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = {
                        reportViewModel.submitReport(reportedUid, reportedName, reporterName, reason, details)
                        submitted = true
                    },
                    enabled = reason.isNotBlank()
                ) { Text("Submit") }
            } else {
                Button(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (!submitted) TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}