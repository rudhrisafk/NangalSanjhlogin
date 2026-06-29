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
    val roles: List<String> = emptyList(),
    val category: String = "",
    val bloodGroup: String = "",
    val age: String = "",                 // NEW
    val isDonor: Boolean = false,          // NEW
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

    private val _activeRole = MutableStateFlow("")
    val activeRole: StateFlow<String> = _activeRole

    private val _navigateToWorkerSetup = MutableStateFlow(false)
    val navigateToWorkerSetup: StateFlow<Boolean> = _navigateToWorkerSetup

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
                    @Suppress("UNCHECKED_CAST")
                    val roles = (doc.get("roles") as? List<String>) ?: listOf(doc.getString("role") ?: "seeker")
                    val activeRole = doc.getString("activeRole") ?: doc.getString("role") ?: "seeker"

                    _profile.value = UserProfile(
                        uid = uid,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        address = doc.getString("address") ?: "",
                        role = activeRole,
                        roles = roles,
                        category = doc.getString("category") ?: "",
                        bloodGroup = doc.getString("bloodGroup") ?: "",
                        age = doc.getString("age") ?: "",
                        isDonor = doc.getBoolean("isDonor") ?: false,
                        jobsDone = (doc.getLong("jobsDone") ?: 0L).toInt()
                    )
                    _activeRole.value = activeRole
                    fetchCounts(uid)
                }
                _isLoading.value = false
            }
    }

    private fun fetchCounts(uid: String) {
        db.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                _profile.value = _profile.value.copy(postsCount = snapshot?.documents?.size ?: 0)
            }

        db.collection("complaints")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                _profile.value = _profile.value.copy(complaintsCount = snapshot?.documents?.size ?: 0)
            }
    }

    fun triggerWorkerSetup() {
        _navigateToWorkerSetup.value = true
    }

    fun resetWorkerSetupTrigger() {
        _navigateToWorkerSetup.value = false
    }

    fun addWorkerProfile(category: String, price: String, about: String, aadhar: String) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        val currentRoles = _profile.value.roles.toMutableList()
        if (!currentRoles.contains("worker")) currentRoles.add("worker")

        db.collection("users").document(uid)
            .update(mapOf(
                "roles" to currentRoles,
                "activeRole" to "worker",
                "category" to category,
                "price" to price,
                "about" to about,
                "aadhar" to aadhar
            ))
            .addOnSuccessListener {
                _isLoading.value = false
                _updateSuccess.value = true
                _activeRole.value = "worker"
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
            }
    }

    fun switchToRole(newRole: String) {
        val uid = auth.currentUser?.uid ?: return
        if (newRole == activeRole.value) return

        _isLoading.value = true

        val currentRoles = _profile.value.roles.toMutableList()
        val updates: MutableMap<String, Any> = mutableMapOf("activeRole" to newRole)

        if (!currentRoles.contains(newRole)) {
            currentRoles.add(newRole)
            updates["roles"] = currentRoles
        }

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                _isLoading.value = false
                _activeRole.value = newRole
                _profile.value = _profile.value.copy(
                    role = newRole,
                    roles = currentRoles
                )
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage
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

    // UPDATED: ab age bhi save karta hai, aur isDonor false hone par blood_donors se entry hata bhi deta hai
    fun updateBloodGroupAndAge(bloodGroup: String, age: String, isDonor: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val name = _profile.value.name
        val phone = _profile.value.phone
        _isLoading.value = true

        db.collection("users").document(uid)
            .update(mapOf(
                "bloodGroup" to bloodGroup,
                "age" to age,
                "isDonor" to isDonor
            ))
            .addOnSuccessListener {
                db.collection("blood_donors")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (isDonor && bloodGroup.isNotEmpty()) {
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
                                snapshot.documents[0].reference.update("bloodGroup", bloodGroup)
                            }
                        } else {
                            // Donor consent hata diya -> blood_donors se bhi entry hata do
                            if (!snapshot.isEmpty) {
                                snapshot.documents[0].reference.delete()
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

    // Purana function rakha hai backward-compatibility ke liye (kahin aur use ho raha ho to na toote)
    fun updateBloodGroup(bloodGroup: String, isDonor: Boolean) {
        updateBloodGroupAndAge(bloodGroup, _profile.value.age, isDonor)
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }
}