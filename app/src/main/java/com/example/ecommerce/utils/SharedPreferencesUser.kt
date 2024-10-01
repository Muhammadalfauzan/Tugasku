package com.example.ecommerce.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class SharedPreferencesUser(private val context: Context) {

    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private  val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveFingerprintStatus(email: String, isFingerprintEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("fingerprint_status_$email", isFingerprintEnabled).apply()
        Log.d("SharedPrefsDebug", "Fingerprint status for $email saved: $isFingerprintEnabled")
    }

    fun getFingerprintStatus(email: String): Boolean {
        val status = sharedPreferences.getBoolean("fingerprint_status_$email", false)
        Log.d("SharedPrefsDebug", "Fingerprint status for $email retrieved: $status")
        return status
    }

    fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
        Log.d("SharedPrefsDebug", "Login status saved: $isLoggedIn")
    }

    fun getLoginStatus(): Boolean {
        val status = sharedPreferences.getBoolean("isLoggedIn", false)
        Log.d("SharedPrefsDebug", "Login status retrieved: $status")
        return status
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
        Log.d("SharedPrefsDebug", "User email saved: $email")
    }

    fun getUserEmail(): String {
        val email = sharedPreferences.getString("user_email", "") ?: ""
        Log.d("SharedPrefsDebug", "User email retrieved: $email")
        return email
    }
    // Fungsi tambahan untuk mendapatkan semua data di SharedPreferences (hanya untuk debugging)
    fun getAllPreferences(): Map<String, *> {
        return sharedPreferences.all
    }
    // Simpan Google Id Token

    // Simpan Google Id Token dan tambahkan log
    fun saveGoogleIdToken(idToken: String) {
        sharedPreferences.edit().putString("google_id_token", idToken).apply()
        Log.d("SharedPrefsDebug", "Google ID Token saved: $idToken")
    }

    // Ambil Google Id Token dan tambahkan log
    fun getGoogleIdToken(): String {
        val idToken = sharedPreferences.getString("google_id_token", "") ?: ""
        Log.d("SharedPrefsDebug", "Google ID Token retrieved: $idToken")
        return idToken
    }

    fun saveUserDisplayName(displayName: String) {
        sharedPreferences.edit().putString("user_display_name", displayName).apply()
        Log.d("EncryptedPrefs", "User display name saved: $displayName")
    }

    fun getUserDisplayName(): String? {
        return sharedPreferences.getString("user_display_name", null)
    }

    fun saveUserPhotoUrl(photoUrl: String) {
        sharedPreferences.edit().putString("user_photo_url", photoUrl).apply()
        Log.d("EncryptedPrefs", "User photo URL saved: $photoUrl")
    }

    fun getUserPhotoUrl(): String? {
        return sharedPreferences.getString("user_photo_url", null)
    }

    // Hapus semua data di SharedPreferences
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
        Log.d("SharedPrefsDebug", "SharedPreferences cleared successfully")
    }
}


