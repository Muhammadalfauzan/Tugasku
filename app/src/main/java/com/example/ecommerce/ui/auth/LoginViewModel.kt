package com.example.ecommerce.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ecommerce.utils.SharedPreferencesUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val firebaseAuth: FirebaseAuth
) : AndroidViewModel(application) {

    // LiveData untuk memantau status login dan data pengguna
    val authState = MutableLiveData<Boolean>()  // Status login pengguna
    val userDisplayName = MutableLiveData<String?>()  // Nama pengguna
    val userEmail = MutableLiveData<String?>()  // Email pengguna
    val userPhotoUrl = MutableLiveData<String?>()  // URL foto pengguna
    val fingerprintStatus = MutableLiveData<Boolean>()  // Status fingerprint

    private val sharedPreferencesManager = SharedPreferencesUser(getApplication<Application>())

    init {
        // Listener untuk memantau perubahan status login
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Pengguna sudah login, perbarui data dan status
                updateUserStateFromFirebase(user)
                authState.value = true
            } else {
                // Pengguna tidak login, set authState ke false
                clearUserState()
                authState.value = false
            }
        }

        // Inisialisasi status fingerprint dari SharedPreferences
        checkFingerprintStatus()
    }

    // Fungsi untuk mengecek status login dan data pengguna
    fun checkIfUserIsLoggedIn() {
        val firebaseUser = firebaseAuth.currentUser
        val isLoggedIn = sharedPreferencesManager.getLoginStatus()

        if (firebaseUser != null && isLoggedIn) {
            // Pengguna sudah login
            updateUserStateFromFirebase(firebaseUser)
            authState.value = true
        } else {
            // Tidak ada pengguna yang login
            clearUserState()
            authState.value = false
        }
    }

    // Fungsi untuk sinkronisasi data pengguna dari FirebaseUser ke LiveData dan SharedPreferences
    private fun updateUserStateFromFirebase(user: FirebaseUser) {
        // Ambil data dari FirebaseUser atau SharedPreferences jika null
        val email = user.email ?: sharedPreferencesManager.getUserEmail()
        val displayName = user.displayName ?: sharedPreferencesManager.getUserDisplayName()
        val photoUrl = user.photoUrl?.toString() ?: sharedPreferencesManager.getUserPhotoUrl()

        // Perbarui LiveData (nilai bisa null karena MutableLiveData bersifat nullable)
        userEmail.value = email
        userDisplayName.value = displayName
        userPhotoUrl.value = photoUrl

        // Simpan ke SharedPreferences untuk menjaga sinkronisasi data
        sharedPreferencesManager.saveUserEmail(email ?: "")
        sharedPreferencesManager.saveUserDisplayName(displayName ?: "")
        sharedPreferencesManager.saveUserPhotoUrl(photoUrl ?: "")

        // Perbarui status fingerprint dari SharedPreferences
        checkFingerprintStatus()
    }
    // Fungsi untuk menghapus data pengguna dari LiveData dan SharedPreferences
    private fun clearUserState() {
        // Hapus status login
        sharedPreferencesManager.saveLoginStatus(false)

        // Reset LiveData
        userEmail.value = null
        userDisplayName.value = null
        userPhotoUrl.value = null
    }

    // Fungsi untuk login menggunakan akun Google
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d("LoginViewModel", "GoogleSignInAccount received: ${account.displayName}, ${account.email}, ${account.idToken}")

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginViewModel", "Firebase Sign-In successful")

                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Simpan Google ID Token ke SharedPreferences
                    val googleIdToken = account.idToken ?: ""
                    sharedPreferencesManager.saveGoogleIdToken(googleIdToken)
                    Log.d("LoginViewModel", "Google ID Token saved: $googleIdToken")

                    // Simpan data user lain ke SharedPreferences
                    sharedPreferencesManager.saveLoginStatus(true)
                    sharedPreferencesManager.saveUserEmail(user.email ?: "")
                    sharedPreferencesManager.saveUserDisplayName(user.displayName ?: "")
                    sharedPreferencesManager.saveUserPhotoUrl(user.photoUrl.toString() ?: "")

                    // Perbarui authState setelah SharedPreferences di-update
                    authState.value = true
                    Log.d("LoginViewModel", "authState set to true after successful login")
                }
            } else {
                Log.e("LoginViewModel", "Login Failed: ${task.exception?.message}")
                authState.value = false
            }
        }
    }


    // Fungsi untuk logout
    fun logoutUser() {
        sharedPreferencesManager.saveLoginStatus(false)
        firebaseAuth.signOut()
        clearUserState()
        authState.value = false
    }

    // Fungsi untuk mengecek status fingerprint di SharedPreferences
    fun checkFingerprintStatus() {
        val uid = firebaseAuth.currentUser?.uid ?: ""
        val isFingerprintEnabled = sharedPreferencesManager.getFingerprintStatus(uid)
        fingerprintStatus.value = isFingerprintEnabled
    }

    // Fungsi untuk memperbarui status fingerprint di SharedPreferences dan LiveData
    fun updateFingerprintStatus(isEnabled: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: ""
        sharedPreferencesManager.saveFingerprintStatus(uid, isEnabled)
        fingerprintStatus.value = isEnabled
    }
}


