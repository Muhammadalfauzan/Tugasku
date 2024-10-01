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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPrefsUser = SharedPreferencesUser(this)
        setupGoogleSignIn()
        // Inisialisasi ImageButton untuk fingerprint
        fingerprintButton = findViewById(R.id.imageButton)

        // Periksa status login dari LoginViewModel
        observeLoginState()

        // Periksa status fingerprint dari SharedPreferences
        checkFingerprintStatus()
    }

    private fun observeLoginState() {
        loginViewModel.authState.observe(this) { isLoggedIn ->
            Log.d("LoginActivity", "Observed authState: $isLoggedIn")

            if (isLoggedIn == true) {
                Log.d("LoginActivity", "User is logged in, navigating to Home")
                navigateToHome()
            } else {
                Log.d("LoginActivity", "User is not logged in, stay in LoginActivity")
            }
        }
    }


    private fun checkFingerprintStatus() {
        // Ambil email dari SharedPreferences
        val email = sharedPrefsUser.getUserEmail()

        Log.d("FingerprintDebug", "Checking fingerprint status for email: $email")

        // Pastikan email tidak kosong sebelum memeriksa status fingerprint
        if (email.isEmpty()) {
            Log.d("FingerprintDebug", "Email is empty, unable to check fingerprint status")
            fingerprintButton.visibility = View.GONE
            return
        }

        // Periksa apakah fingerprint aktif untuk pengguna saat ini
        val isFingerprintEnabled = sharedPrefsUser.getFingerprintStatus(email)

        Log.d("FingerprintDebug", "Is fingerprint enabled: $isFingerprintEnabled")

        // Periksa apakah perangkat mendukung biometric dan fingerprint aktif
        val isBiometricReady = BiometricUtils.isBiometricReady(this)
        Log.d("FingerprintDebug", "Is biometric ready: $isBiometricReady")

        if (isFingerprintEnabled == true && isBiometricReady) {
            // Jika fingerprint aktif dan perangkat mendukung, tampilkan tombol
            fingerprintButton.visibility = View.VISIBLE
            Log.d("FingerprintDebug", "Fingerprint button is now visible")

            fingerprintButton.setOnClickListener {
                // Tampilkan prompt biometric untuk otentikasi
                BiometricUtils.showBiometricPrompt(this, this)
                Log.d("FingerprintDebug", "Fingerprint prompt shown")
            }
        } else {
            // Jika fingerprint tidak aktif atau perangkat tidak mendukung, sembunyikan tombol
            fingerprintButton.visibility = View.GONE
            Log.d(
                "FingerprintDebug",
                "Fingerprint button is hidden because fingerprint is disabled or biometric not ready"
            )
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
                Log.d("LoginActivity", "Google Sign-In successful, Account: ${account.email}")
                loginViewModel.firebaseAuthWithGoogle(account)  // Panggil metode login di ViewModel
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google Sign-In failed: ${e.message}")
            }
        }
    }

    // Implementasi listener untuk Biometric Prompt
    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(this, "Biometric Authentication Error: $errMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        // Ambil Google ID Token dari SharedPreferences
        val googleIdToken = sharedPrefsUser.getGoogleIdToken()
        Log.d(
            "FingerprintDebug",
            "Google ID Token retrieved after fingerprint authentication: $googleIdToken"
        )

        if (googleIdToken.isNotEmpty()) {
            Log.d("FingerprintDebug", "Attempting to login with Google ID Token")
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(
                            "FingerprintDebug",
                            "Login successful with Google account after fingerprint authentication."
                        )

                        // Ambil informasi pengguna setelah login berhasil
                        val user = FirebaseAuth.getInstance().currentUser

                        // Perbarui status login di SharedPreferences
                        sharedPrefsUser.saveLoginStatus(true)
                        Log.d("FingerprintDebug", "Login status updated in SharedPreferences: true")

                        // Perbarui email pengguna di SharedPreferences jika perlu
                        if (user != null) {
                            sharedPrefsUser.saveUserEmail(user.email ?: "")
                            Log.d(
                                "FingerprintDebug",
                                "User email saved in SharedPreferences: ${user.email}"
                            )
                        }

                        // Navigasi ke profil atau home
                        navigateToHome()
                    } else {
                        Log.e(
                            "FingerprintDebug",
                            "Login failed after fingerprint authentication: ${task.exception?.message}"
                        )
                        Toast.makeText(
                            this,
                            "Login failed. Please login again with your Google account.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Log.e("FingerprintDebug", "Google ID Token is missing. Cannot login to Firebase.")
            Toast.makeText(
                this,
                "Cannot login with fingerprint. Please login again with your Google account.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun navigateToHome() {
        // Arahkan ke halaman utama setelah login berhasil
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    // Tambahkan metode onResume() di sini untuk memeriksa ulang status fingerprint setiap kali activity muncul
    override fun onResume() {
        super.onResume()
        // Jangan lakukan navigasi di onResume, hanya cek status login
        val isLoggedIn = sharedPrefsUser.getLoginStatus()
        Log.d("LoginActivity", "Login status in onResume: $isLoggedIn")
    }
}