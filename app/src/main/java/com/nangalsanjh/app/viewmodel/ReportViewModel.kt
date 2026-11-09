// ReportViewModel.kt — NEW FILE

package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Report(
    val id: String = "",
    val reportedUid: String = "",
    val reportedName: String = "",
    val reporterUid: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val details: String = "",
    val status: String = "pending", // pending | reviewed
    val timeStamp: Long = 0L
)

class ReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports

    private val _blockedUids = MutableStateFlow<Set<String>>(emptySet())
    val blockedUids: StateFlow<Set<String>> = _blockedUids

    init {
        fetchReports()
        fetchBlockedUsers()
    }

    fun fetchReports() {
        db.collection("reports")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                _reports.value = snapshot?.documents?.mapNotNull { doc ->
                    Report(
                        id = doc.id,
                        reportedUid = doc.getString("reportedUid") ?: "",
                        reportedName = doc.getString("reportedName") ?: "",
                        reporterUid = doc.getString("reporterUid") ?: "",
                        reporterName = doc.getString("reporterName") ?: "",
                        reason = doc.getString("reason") ?: "",
                        details = doc.getString("details") ?: "",
                        status = doc.getString("status") ?: "pending",
                        timeStamp = doc.getLong("timeStamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun fetchBlockedUsers() {
        db.collection("users").whereEqualTo("isBlocked", true)
            .addSnapshotListener { snapshot, _ ->
                _blockedUids.value = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
            }
    }

    fun submitReport(reportedUid: String, reportedName: String, reporterName: String, reason: String, details: String) {
        val reporterUid = auth.currentUser?.uid ?: return
        db.collection("reports").add(
            hashMapOf(
                "reportedUid" to reportedUid,
                "reportedName" to reportedName,
                "reporterUid" to reporterUid,
                "reporterName" to reporterName,
                "reason" to reason,
                "details" to details,
                "status" to "pending",
                "timeStamp" to System.currentTimeMillis()
            )
        )
        db.collection("admin_notifications").add(
            hashMapOf(
                "type" to "report",
                "message" to "$reporterName reported $reportedName: $reason",
                "isRead" to false,
                "timeStamp" to System.currentTimeMillis()
            )
        )
    }

    fun blockUser(uid: String) {
        db.collection("users").document(uid).update("isBlocked", true)
    }

    fun unblockUser(uid: String) {
        db.collection("users").document(uid).update("isBlocked", false)
    }

    fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).update("status", "reviewed")
    }
}