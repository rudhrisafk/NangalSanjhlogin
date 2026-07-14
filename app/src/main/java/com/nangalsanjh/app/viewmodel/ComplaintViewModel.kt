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
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
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

    init { fetchComplaints() }

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
                    ComplaintModel(
                        id = doc.id,
                        reporter = doc.getString("reporter") ?: "",
                        userPhone = doc.getString("userPhone") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        timeStamp = doc.getLong("timeStamp") ?: 0L,
                        status = doc.getString("status") ?: "Pending",
                        departmentUpdate = doc.getString("departmentUpdate") ?: "",
                        uid = doc.getString("uid") ?: ""
                    )
                } ?: emptyList()
                _complaints.value = list
            }
    }

    fun submitComplaint(
        reporter: String,
        category: String,
        description: String,
        location: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
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
            "latitude" to latitude,
            "longitude" to longitude,
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
                _error.value = e.localizedMessage ?: "Failed to submit"
            }
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: String,
        departmentUpdate: String
    ) {
        db.collection("complaints").document(complaintId)
            .update(mapOf("status" to newStatus, "departmentUpdate" to departmentUpdate))
    }

    fun deleteComplaint(complaintId: String) {
        db.collection("complaints").document(complaintId).delete()
    }

    fun resetSubmitSuccess() {
        _submitSuccess.value = false
    }
}