package com.example.ecommerce.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ecommerce.utils.SharedPreferencesUser
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

    val authState = MutableLiveData<Boolean>()
    val userDisplayName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()
    val userPhotoUrl = MutableLiveData<String>()

    val sharedPreferencesManager = SharedPreferencesUser(getApplication<Application>())
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("LoginViewModel", "GoogleSignInAccount received: ${account.displayName}, ${account.email}, ${account.idToken}")

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginViewModel", "Firebase Sign-In successful")

                // simpan informasi user
                val user = firebaseAuth.currentUser

                if (user != null) {
                    Log.d("LoginViewModel", "Firebase User info: Name=${user.displayName}, Email=${user.email}, Photo=${user.photoUrl}")

                    authState.value = true
                    userDisplayName.value = user.displayName
                    userEmail.value = user.email
                    userPhotoUrl.value = user.photoUrl.toString()

                    // Simpan data user ke EncryptedSharedPreferences
                    sharedPreferencesManager.saveLoginStatus(true)
                    sharedPreferencesManager.saveUserEmail(user.email ?: "")
                    sharedPreferencesManager.saveUserDisplayName(user.displayName ?: "")
                    sharedPreferencesManager.saveUserPhotoUrl(user.photoUrl.toString() ?: "")
                } else {
                    Log.e("LoginViewModel", "Firebase User is null after sign-in")
                }
            } else {
                Log.e("LoginViewModel", "Login Failed: ${task.exception?.message}")
                authState.value = false
            }
        }
    }

    // Cek user sudah login
    fun checkIfUserIsLoggedIn() {
        val isLoggedIn = sharedPreferencesManager.getLoginStatus()
        Log.d("EncryptedPrefs", "Login status saved: true")
        if (isLoggedIn) {
            Log.d("LoginViewModel", "User is already logged in")

            authState.value = true
            userDisplayName.value = sharedPreferencesManager.getUserDisplayName()
            Log.d("EncryptedPrefs", "User display name loaded: $userDisplayName")
            userEmail.value = sharedPreferencesManager.getUserEmail()
            Log.d("EncryptedPrefs", "User photo URL loaded: $userEmail")
            userPhotoUrl.value = sharedPreferencesManager.getUserPhotoUrl()
            Log.d("EncryptedPrefs", "User photo URL loaded: $userPhotoUrl")
        } else {
            Log.d("LoginViewModel", "No user is currently logged in")
            authState.value = false
        }
    }

    fun signOut() {
        Log.d("LoginViewModel", "User signed out")
        firebaseAuth.signOut()

        sharedPreferencesManager.clearUserData()
        authState.value = false
    }
}
