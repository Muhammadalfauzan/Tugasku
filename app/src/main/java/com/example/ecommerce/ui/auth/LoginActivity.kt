package com.example.ecommerce.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), BiometricAuthListener {

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private lateinit var sharedPreferencesManager: SharedPreferencesUser

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferencesManager = SharedPreferencesUser(this)

        // Cek apakah user sudah pernah mendaftarkan sidik jari
        if (sharedPreferencesManager.getLoginStatus() && BiometricUtils.isBiometricReady(this)) {
            // Tidak tampilkan fingerprint jika aplikasi pertama kali dibuka.
            // Tampilkan hanya jika user sudah pernah login dan logout sebelumnya
            BiometricUtils.showBiometricPrompt(
                activity = this,
                listener = this,
                cryptoObject = null
            )
        } else {
            setupGoogleSignIn()
        }

        val fingerprintButton = findViewById<ImageButton>(R.id.imageButton)
        fingerprintButton.setOnClickListener {
            if (BiometricUtils.isBiometricReady(this)) {
                BiometricUtils.showBiometricPrompt(
                    activity = this,
                    listener = this,
                    cryptoObject = null
                )
            } else {
                Toast.makeText(this, "No biometric feature available on this device", Toast.LENGTH_SHORT).show()
            }
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
                // Simpan data pengguna ke SharedPreferences setelah login Google berhasil
                sharedPreferencesManager.saveUserEmail(account.email ?: "")
                sharedPreferencesManager.saveLoginStatus(false)  // Tidak aktifkan login sidik jari saat pertama kali
                Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
                navigateToProfile()
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Implementasi listener untuk Biometric Prompt
    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(this, "Biometric Authentication Error: $errMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        // Langsung navigasi ke halaman profil jika fingerprint berhasil
        navigateToProfile()
    }

    private fun navigateToProfile() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}



