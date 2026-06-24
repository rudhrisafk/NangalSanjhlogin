package com.nangalsanjh.app.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _savedRole = MutableStateFlow("")
    val savedRole: StateFlow<String> = _savedRole

    private val _savedName = MutableStateFlow("")
    val savedName: StateFlow<String> = _savedName

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _otpSent = MutableStateFlow(value = false)
    val otpSent: StateFlow<Boolean> = _otpSent

    private val _lastPhone = MutableStateFlow("")
    val lastPhone: StateFlow<String> = _lastPhone

    private val _verificationComplete = MutableStateFlow(value = false)
    val verificationComplete: StateFlow<Boolean> = _verificationComplete

    private val _profileSaved = MutableStateFlow(value = false)
    val profileSaved: StateFlow<Boolean> = _profileSaved

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun sendOTP(phoneNumber: String, activity: Activity) {
        _isLoading.value = true
        _error.value = null
        _lastPhone.value = phoneNumber

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
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
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        _isLoading.value = false
                        this@AuthViewModel.verificationId = verificationId
                        resendToken = token
                        _otpSent.value = true
                    }
                },
            )
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

    fun saveUserProfile(name: String, address: String, role: String, category: String = "") {
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
            "category" to category,
            "createdAt" to System.currentTimeMillis(),
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                _isLoading.value = false
                _profileSaved.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.localizedMessage ?: "Failed to save profile"
            }
    }

    fun logout() {
        auth.signOut()
        _verificationComplete.value = false
        _otpSent.value = false
        _profileSaved.value = false
        _savedName.value = ""
        _savedRole.value = ""
        _lastPhone.value = ""
        verificationId = null
        _error.value = null
        _isLoading.value = false
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
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _error.value = e.localizedMessage
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        this@AuthViewModel.verificationId = verificationId
                        resendToken = token
                        _otpSent.value = true
                    }
                },
            )
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
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