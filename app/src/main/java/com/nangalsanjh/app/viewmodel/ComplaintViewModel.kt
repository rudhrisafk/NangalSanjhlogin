package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ComplaintModel(
    val id: String = "",
    val reporter: String = "",
    val userPhone: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val timeStamp: Long = 0L,
    val status: String = "Pending",
    val departmentUpdate: String = "",
    val uid: String = ""
)

class ComplaintViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _complaints = MutableStateFlow<List<ComplaintModel>>(emptyList())
    val complaints: StateFlow<List<ComplaintModel>> = _complaints

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    init {
        fetchComplaints()
    }

    fun fetchComplaints() {
        _isLoading.value = true
        db.collection("complaints")
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.localizedMessage
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ComplaintModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _complaints.value = list
            }
    }

    fun submitComplaint(
        reporter: String,
        category: String,
        description: String,
        location: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""

        _isLoading.value = true
        _error.value = null

        val complaint = hashMapOf(
            "reporter" to reporter,
            "userPhone" to phone,
            "category" to category,
            "description" to description,
            "location" to location,
            "timeStamp" to System.currentTimeMillis(),
            "status" to "Pending",
            "departmentUpdate" to "",
            "uid" to uid
        )

        db.collection("complaints")
            .add(complaint)
            .addOnSuccessListener {
                _isLoading.value = false
                _submitSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to submit complaint"
            }
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: String,
        departmentUpdate: String
    ) {
        db.collection("complaints").document(complaintId)
            .get()
            .addOnSuccessListener { doc ->
                val reporterUid = doc.getString("uid") ?: return@addOnSuccessListener
                val category = doc.getString("category") ?: ""

                // Status update karo
                db.collection("complaints").document(complaintId)
                    .update(
                        mapOf(
                            "status" to newStatus,
                            "departmentUpdate" to departmentUpdate
                        )
                    )

                // Reporter ka FCM token lo aur notification bhejo
                db.collection("users").document(reporterUid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val fcmToken = userDoc.getString("fcmToken") ?: return@addOnSuccessListener
                        sendComplaintNotification(fcmToken, category, newStatus)
                    }
            }
    }

    fun resetSubmitSuccess() {
        _submitSuccess.value = false
    }

    private fun sendComplaintNotification(
        token: String,
        category: String,
        status: String
    ) {
        // Firebase Admin SDK se bheja jaata hai
        // Abhi ke liye Firestore mein save karo
        db.collection("notifications").add(
            hashMapOf(
                "token" to token,
                "title" to "Complaint Update 🏛️",
                "body" to "Aapki $category complaint ka status: $status",
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
}