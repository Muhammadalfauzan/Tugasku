package com.example.ecommerce.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class SharedPreferencesUser(private val context: Context) {

    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
        Log.d("EncryptedPrefs", "Login status saved: $isLoggedIn")
    }

    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
        Log.d("EncryptedPrefs", "User email saved: $email")
    }
    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
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

    fun clearUserData() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d("EncryptedPrefs", "EncryptedSharedPreferences cleared successfully")
        } catch (e: Exception) {
            Log.e("EncryptedPrefs", "Error clearing EncryptedSharedPreferences: ${e.message}")
        }
    }
}


