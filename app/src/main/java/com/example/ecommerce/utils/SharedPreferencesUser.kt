package com.example.ecommerce.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SharedPreferencesUser @Inject constructor(
    @ApplicationContext private val context: Context // Menggunakan Hilt injection
) {
    // membuat alias master key untuk enkripsi menggunakan AES256 GCM
    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    //Membuat instance EncryptedSharePreferences
    private  val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_prefs",
        masterKeyAlias, // Mammggil master key yang telah di buat
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // menyimpan status fingerprint berdasarkan email
    fun saveFingerprintStatus(email: String, isFingerprintEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("fingerprint_status_$email", isFingerprintEnabled).apply()
        Log.d("SharedPrefsDebug", "Fingerprint status for $email saved: $isFingerprintEnabled")
    }

    //mengambil status fingerprint berdasarkan emai
    fun getFingerprintStatus(email: String): Boolean {
        val status = sharedPreferences.getBoolean("fingerprint_status_$email", false)
        Log.d("SharedPrefsDebug", "Fingerprint status for $email retrieved: $status")
        return status
    }

    // Menyimpan status login pengguna
    fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
        Log.d("SharedPrefsDebug", "Login status saved: $isLoggedIn")
    }

    // Mengambil status login
    fun getLoginStatus(): Boolean {
        val status = sharedPreferences.getBoolean("isLoggedIn", false)
        Log.d("SharedPrefsDebug", "Login status retrieved: $status")
        return status
    }

    // Menyimpan email
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("user_email", email).apply()
        Log.d("SharedPrefsDebug", "User email saved: $email")
    }

    // Mengambil email
    fun getUserEmail(): String {
        val email = sharedPreferences.getString("user_email", "") ?: ""
        Log.d("SharedPrefsDebug", "User email retrieved: $email")
        return email
    }


    // Menyimpan google id token
    fun saveGoogleIdToken(idToken: String) {
        sharedPreferences.edit().putString("google_id_token", idToken).apply()
        Log.d("SharedPrefsDebug", "Google ID Token saved: $idToken")
    }

    // mengambil google id token
    fun getGoogleIdToken(): String {
        val idToken = sharedPreferences.getString("google_id_token", "") ?: ""
        Log.d("SharedPrefsDebug", "Google ID Token retrieved: $idToken")
        return idToken
    }

    // menyimpan nama tampilan pengguna
    fun saveUserDisplayName(displayName: String) {
        sharedPreferences.edit().putString("user_display_name", displayName).apply()
        Log.d("EncryptedPrefs", "User display name saved: $displayName")
    }
    // mengambil nama
    fun getUserDisplayName(): String? {
        return sharedPreferences.getString("user_display_name", null)
    }

    // menyimpan url foto
    fun saveUserPhotoUrl(photoUrl: String) {
        sharedPreferences.edit().putString("user_photo_url", photoUrl).apply()
        Log.d("EncryptedPrefs", "User photo URL saved: $photoUrl")
    }

    // mengambil url footo
    fun getUserPhotoUrl(): String? {
        return sharedPreferences.getString("user_photo_url", null)
    }

}


