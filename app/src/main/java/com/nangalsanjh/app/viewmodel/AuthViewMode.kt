package com.nangalsanjh.app.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("nangal_prefs", Context.MODE_PRIVATE)

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent

    private val _lastPhone = MutableStateFlow("")
    val lastPhone: StateFlow<String> = _lastPhone

    private val _verificationComplete = MutableStateFlow(false)
    val verificationComplete: StateFlow<Boolean> = _verificationComplete

    private val _profileSaved = MutableStateFlow(false)
    val profileSaved: StateFlow<Boolean> = _profileSaved

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _savedRole = MutableStateFlow("")
    val savedRole: StateFlow<String> = _savedRole

    private val _savedName = MutableStateFlow("")
    val savedName: StateFlow<String> = _savedName

    private val _bloodGroup = MutableStateFlow("")
    val bloodGroup: StateFlow<String> = _bloodGroup

    private val _age = MutableStateFlow("")
    val age: StateFlow<String> = _age

    private val _donorConsent = MutableStateFlow(false)
    val donorConsent: StateFlow<Boolean> = _donorConsent

    // Existing user check — SharedPreferences se
    fun isExistingUser(): Boolean {
        return prefs.getBoolean("is_registered", false)
    }

    fun setBloodInfo(bloodGroup: String, age: String, consent: Boolean) {
        _bloodGroup.value = bloodGroup
        _age.value = age
        _donorConsent.value = consent
    }

    fun sendOTP(phoneNumber: String, activity: Activity) {
        _isLoading.value = true
        _error.value = null
        _lastPhone.value = phoneNumber

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    _isLoading.value = false
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isLoading.value = false
                    _error.value = e.localizedMessage
                    Log.e("FirebaseOTP", "Verification Failed", e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _isLoading.value = false
                    this@AuthViewModel.verificationId = verificationId
                    resendToken = token
                    _otpSent.value = true
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOTP(code: String) {
        val id = verificationId
        if (id == null) {
            _error.value = "Verification ID not found."
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _verificationComplete.value = true
                } else {
                    _error.value = task.exception?.localizedMessage ?: "Authentication Failed"
                }
            }
    }

    fun saveUserProfile(
        name: String,
        address: String,
        role: String,
        category: String = "",
        price: String = "",
        about: String = "",
        aadhar: String = ""
    ) {
        _isLoading.value = true
        _savedName.value = name
        _savedRole.value = role

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "User not logged in"
            _isLoading.value = false
            return
        }
        val phone = auth.currentUser?.phoneNumber ?: ""

        val userData = hashMapOf(
            "uid" to uid,
            "name" to name,
            "phone" to phone,
            "address" to address,
            "role" to role,
            "activeRole" to role,
            "roles" to listOf(role),
            "category" to category,
            "price" to price,
            "about" to about,
            "aadhar" to aadhar,
            "bloodGroup" to _bloodGroup.value,
            "age" to _age.value,
            "isDonor" to (_donorConsent.value && _bloodGroup.value.isNotEmpty()),
            "isAvailable" to false,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                // Registered flag save karo
                prefs.edit().putBoolean("is_registered", true).apply()

                if (_donorConsent.value && _bloodGroup.value.isNotEmpty()) {
                    saveToDonorsList(uid, name, phone, _bloodGroup.value, address)
                }
                _isLoading.value = false
                _profileSaved.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to save profile"
            }
    }

    private fun saveToDonorsList(
        uid: String,
        name: String,
        phone: String,
        bloodGroup: String,
        area: String
    ) {
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
                            "area" to area,
                            "createdAt" to System.currentTimeMillis()
                        )
                    )
                }
            }
    }

    fun resendOTP(phoneNumber: String, activity: Activity) {
        val token = resendToken
        if (token == null) {
            sendOTP(phoneNumber, activity)
            return
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setForceResendingToken(token)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _error.value = e.localizedMessage
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@AuthViewModel.verificationId = verificationId
                    resendToken = token
                    _otpSent.value = true
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun logout() {
        auth.signOut()
        _verificationComplete.value = false
        _otpSent.value = false
        _profileSaved.value = false
        _savedName.value = ""
        _savedRole.value = ""
        _lastPhone.value = ""
        _bloodGroup.value = ""
        _age.value = ""
        _donorConsent.value = false
        verificationId = null
        _error.value = null
        _isLoading.value = false
        // NOTE: is_registered flag nahi hatate — taaki dobara login pe blood group na puchein
    }

    fun resetOtpSent() {
        _otpSent.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun resetState() {
        _verificationComplete.value = false
        _otpSent.value = false
        _isLoading.value = false
        _error.value = null
    }

    fun resetProfileSaved() {
        _profileSaved.value = false
    }
}