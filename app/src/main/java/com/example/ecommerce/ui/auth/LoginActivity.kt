package com.example.ecommerce.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.example.ecommerce.MainActivity
import com.example.ecommerce.R
import com.example.ecommerce.utils.SharedPreferencesUser
import com.example.ecommerce.utils.biometric.BiometricAuthListener
import com.example.ecommerce.utils.biometric.BiometricUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), BiometricAuthListener {

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private lateinit var sharedPrefsUser: SharedPreferencesUser
    private lateinit var fingerprintButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPrefsUser = SharedPreferencesUser(this)
        setupGoogleSignIn()

        // Inisialisasi ImageButton untuk fingerprint
        fingerprintButton = findViewById(R.id.imageButton)
        fingerprintButton.visibility = View.GONE // Sembunyikan tombol fingerprint di awal

        // Observasi status login dari ViewModel
        observeLoginState()

        // Observasi status fingerprint dari ViewModel
        observeFingerprintStatus()

        // Periksa status fingerprint dari SharedPreferences setiap kali Activity dibuka kembali
        checkFingerprintStatus()
    }

    // Observasi perubahan pada authState dari ViewModel
    private fun observeLoginState() {
        loginViewModel.authState.observe(this) { isLoggedIn ->
            if (isLoggedIn == true) {
                navigateToHome()
            }
        }
    }

    // Observasi perubahan pada fingerprintStatus dari ViewModel
    private fun observeFingerprintStatus() {
        loginViewModel.fingerprintStatus.observe(this) { isFingerprintEnabled ->
            if (isFingerprintEnabled) {
                fingerprintButton.visibility = View.VISIBLE
                fingerprintButton.setOnClickListener {
                    if (BiometricUtils.isBiometricReady(this)) {
                        BiometricUtils.showBiometricPrompt(this, this)
                    } else {
                        Toast.makeText(this, "Biometric not supported on this device", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                fingerprintButton.visibility = View.GONE
            }
        }
    }

    // Periksa status fingerprint dari SharedPreferences
    private fun checkFingerprintStatus() {
        val email = sharedPrefsUser.getUserEmail()
        if (email.isNotEmpty()) {
            val isFingerprintEnabled = sharedPrefsUser.getFingerprintStatus(email)
            loginViewModel.updateFingerprintStatus(isFingerprintEnabled)
        } else {
            Log.e("LoginActivity", "Email is missing. Cannot check fingerprint status.")
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        findViewById<SignInButton>(R.id.google_signIn).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                loginViewModel.firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google Sign-In failed: ${e.message}")
            }
        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(this, "Biometric Authentication Error: $errMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        val email = sharedPrefsUser.getUserEmail()
        Log.d("FingerprintDebug", "Attempting to login with email: $email")

        if (email.isNotEmpty()) {
            // Ambil Google ID Token dari SharedPreferences
            val googleIdToken = sharedPrefsUser.getGoogleIdToken()
            if (googleIdToken.isNotEmpty()) {
                Log.d("FingerprintDebug", "Attempting to login with Google ID Token")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FingerprintDebug", "Login successful with Google account after fingerprint authentication.")
                            sharedPrefsUser.saveLoginStatus(true)  // Set login status ke true
                            navigateToHome()
                        } else {
                            Log.e("FingerprintDebug", "Login failed after fingerprint authentication: ${task.exception?.message}")
                            Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("FingerprintDebug", "Google ID Token is missing. Cannot login to Firebase.")
                Toast.makeText(this, "Cannot login with fingerprint. Please login again with your Google account.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("FingerprintDebug", "Email is missing. Cannot login to Firebase.")
            Toast.makeText(this, "Cannot login with fingerprint. Please login again with your Google account.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Jangan lakukan navigasi di onResume, hanya cek status login dan fingerprint
        checkFingerprintStatus()
    }
}
