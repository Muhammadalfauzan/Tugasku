package com.example.ecommerce.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    // LiveData to track authentication state
    val authState = MutableLiveData<Boolean>()
    val userDisplayName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()
    val userPhotoUrl = MutableLiveData<String>()

    // Fungsi untuk mengautentikasi Google Sign-In dengan Firebase
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Jika login berhasil, update LiveData dengan informasi user
                authState.value = true
                val user = firebaseAuth.currentUser
                userDisplayName.value = user?.displayName
                userEmail.value = user?.email
                userPhotoUrl.value = user?.photoUrl.toString()
            } else {
                authState.value = false
                Log.e("LoginViewModel", "Login Failed: ${task.exception?.message}")
            }
        }
    }

    // Cek apakah user sudah login sebelumnya
    fun checkIfUserIsLoggedIn() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            authState.value = true
            userDisplayName.value = user.displayName
            userEmail.value = user.email
            userPhotoUrl.value = user.photoUrl.toString()
        } else {
            authState.value = false
        }
    }

    // Fungsi untuk Logout
    fun signOut() {
        firebaseAuth.signOut()
        authState.value = false
    }
}