@file:Suppress("DEPRECATION")

package com.example.ecommerce.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.example.ecommerce.utils.SharedPreferencesUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    val authState = MutableLiveData<Boolean>()
    val userDisplayName = MutableLiveData<String?>()
    val userEmail = MutableLiveData<String?>()
    val userPhotoUrl = MutableLiveData<String?>()
    val fingerprintStatus = MutableLiveData<Boolean>().apply { value = false }

    private val sharedPreferencesManager = SharedPreferencesUser(getApplication<Application>())

    init {
        checkIfUserIsLoggedIn()
    }



    // Cek apakah pengguna sudah login
    fun checkIfUserIsLoggedIn() {
        val isLoggedIn = sharedPreferencesManager.getLoginStatus()
        val email = sharedPreferencesManager.getUserEmail()

        if (isLoggedIn && email.isNotEmpty()) {
            authState.value = true
            userDisplayName.value = sharedPreferencesManager.getUserDisplayName()
            userEmail.value = email
            userPhotoUrl.value = sharedPreferencesManager.getUserPhotoUrl()
            Log.d("LoginViewModel", "User is logged in with email: $email")
            checkFingerprintStatus()  // Cek status fingerprint
        } else {
            authState.value = false
            Log.d("LoginViewModel", "User is not logged in")
        }
    }

    // Cek status fingerprint dari SharedPreferences
    fun checkFingerprintStatus() {
        val email = sharedPreferencesManager.getUserEmail()
        if (email.isNotEmpty()) {
            val isFingerprintEnabled = sharedPreferencesManager.getFingerprintStatus(email)
            fingerprintStatus.value = isFingerprintEnabled
            Log.d("LoginViewModel", "Fingerprint status for email $email: $isFingerprintEnabled")
        } else {
            fingerprintStatus.value = false
            Log.e("LoginViewModel", "Email is empty. Cannot check fingerprint status.")
        }
    }

    // Perbarui status fingerprint di SharedPreferences dan LiveData
    fun updateFingerprintStatus(isEnabled: Boolean) {
        val email = sharedPreferencesManager.getUserEmail()
        if (email.isNotEmpty()) {
            sharedPreferencesManager.saveFingerprintStatus(email, isEnabled)
            fingerprintStatus.value = isEnabled
            Log.d("LoginViewModel", "Fingerprint status updated for email $email: $isEnabled")
        } else {
            Log.e("LoginViewModel", "Email is empty. Cannot update fingerprint status.")
        }
    }

    // Fungsi untuk login dengan Google Account
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("LoginViewModel", "GoogleSignInAccount received: ${account.displayName}, ${account.email}")

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Simpan Google ID Token ke SharedPreferences
                    val googleIdToken = account.idToken ?: ""
                    if (googleIdToken.isNotEmpty()) {
                        sharedPreferencesManager.saveGoogleIdToken(googleIdToken)
                        Log.d("LoginViewModel", "Google ID Token saved: $googleIdToken")
                    }

                    sharedPreferencesManager.saveUserEmail(user.email ?: "")
                    sharedPreferencesManager.saveUserDisplayName(user.displayName ?: "")
                    sharedPreferencesManager.saveUserPhotoUrl(user.photoUrl?.toString() ?: "")
                    sharedPreferencesManager.saveLoginStatus(true)

                    userEmail.value = user.email
                    userDisplayName.value = user.displayName
                    userPhotoUrl.value = user.photoUrl?.toString()
                    authState.value = true

                    Log.d("LoginViewModel", "Login successful, email: ${user.email}")
                }
            } else {
                Log.e("LoginViewModel", "Login Failed: ${task.exception?.message}")
                authState.value = false
            }
        }
    }

    // Fungsi logout
    fun logoutUser() {
        Log.d("LoginViewModel", "Logging out user and clearing login status in SharedPreferences")

        sharedPreferencesManager.saveLoginStatus(false)  // Hanya set login status ke false, jangan hapus data fingerprint
        firebaseAuth.signOut()  // Logout dari FirebaseAuth

        // Jangan hapus email dan status fingerprint di SharedPreferences
        // sharedPreferencesManager.clearUserData()  // Ini hanya digunakan jika ingin menghapus semua data.

        // Perbarui status di LiveData
        authState.value = false
        Log.d("LoginViewModel", "Logout completed, authState set to false")
    }
}




