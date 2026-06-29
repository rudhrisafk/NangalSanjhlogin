package com.nangalsanjh.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = "",
    val category: String = "",
    val bloodGroup: String = "",
    val postsCount: Int = 0,
    val complaintsCount: Int = 0,
    val jobsDone: Int = 0
)

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { fetchProfile() }

    fun fetchProfile() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("users").document(uid)
            .addSnapshotListener { doc, e ->
                if (e != null) {
                    _error.value = e.localizedMessage
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    val profile = UserProfile(
                        uid = uid,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        address = doc.getString("address") ?: "",
                        role = doc.getString("role") ?: "",
                        category = doc.getString("category") ?: "",
                        bloodGroup = doc.getString("bloodGroup") ?: "",
                        jobsDone = (doc.getLong("jobsDone") ?: 0L).toInt()
                    )
                    _profile.value = profile
                    fetchCounts(uid)
                }
                _isLoading.value = false
            }
    }

    private fun fetchCounts(uid: String) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                _profile.value = _profile.value.copy(
                    postsCount = snapshot?.documents?.size ?: 0
                )
            }

        db.collection("complaints")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                _profile.value = _profile.value.copy(
                    complaintsCount = snapshot?.documents?.size ?: 0
                )
            }
    }

    fun updateProfile(name: String, address: String) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("users").document(uid)
            .update(mapOf("name" to name, "address" to address))
            .addOnSuccessListener {
                _isLoading.value = false
                _updateSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    fun updateCategory(category: String) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.collection("users").document(uid)
            .update("category", category)
            .addOnSuccessListener {
                _isLoading.value = false
                _updateSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    fun updateBloodGroup(bloodGroup: String, isDonor: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val name = _profile.value.name
        val phone = _profile.value.phone
        _isLoading.value = true

        db.collection("users").document(uid)
            .update(mapOf("bloodGroup" to bloodGroup, "isDonor" to isDonor))
            .addOnSuccessListener {
                if (isDonor && bloodGroup.isNotEmpty()) {
                    db.collection("blood_donors")
                        .whereEqualTo("uid", uid)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.isEmpty) {
                                db.collection("blood_donors").add(
                                    hashMapOf(
                                        "uid" to uid,
                                        "name" to name,
                                        "phone" to phone,
                                        "bloodGroup" to bloodGroup,
                                        "area" to _profile.value.address,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                )
                            } else {
                                snapshot.documents[0].reference
                                    .update("bloodGroup", bloodGroup)
                            }
                        }
                }
                _isLoading.value = false
                _updateSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }
}