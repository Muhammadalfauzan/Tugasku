@file:Suppress("DEPRECATION")

package com.example.ecommerce.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import android.provider.Settings
import androidx.cardview.widget.CardView

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), BiometricAuthListener {
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private lateinit var sharedPrefsUser: SharedPreferencesUser
    private lateinit var fingerprintButton: MaterialButton

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
        // Cek izin overlay di onCreate saat Activity pertama kali dibuat
        checkDrawOverlayPermission()
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
        showLoading()
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    @SuppressLint("ObsoleteSdkInt", "SuspiciousIndentation")
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                loginViewModel.firebaseAuthWithGoogle(account)
                hideLoading()
            } catch (e: ApiException) {
                hideLoading()
                Log.e("LoginActivity", "Google Sign-In failed: ${e.message}")
            }
        }else if (requestCode == REQUEST_CODE) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
            Toast.makeText(this,"Overlay permission granted", Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(this,"Overlay permisson is required for this app", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(this, "Biometric Authentication Error: $errMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        showLoading()
        val email = sharedPrefsUser.getUserEmail()
        Log.d("FingerprintDebug", " masuk dengan email: $email")

        if (email.isNotEmpty()) {
            // Ambil Google ID Token dari SharedPreferences
            val googleIdToken = sharedPrefsUser.getGoogleIdToken()
            if (googleIdToken.isNotEmpty()) {
                Log.d("FingerprintDebug", " masuk dengan Token ID Google")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        hideLoading()
                        if (task.isSuccessful) {
                            Log.d("FingerprintDebug", "Login berhasil dengan akun Google setelah otentikasi sidik jari.")
                            sharedPrefsUser.saveLoginStatus(true)  // Set login status ke true
                            navigateToHome()
                        } else {
                            Log.e("FingerprintDebug", "Login gagal setelah otentikasi sidik jari: ${task.exception?.message}")
                            Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("FingerprintDebug", "Token tidak dapa di gunakan ")
                Toast.makeText(this, "Tidak dapat masuk dengan sidik jari. Silakan login kembali dengan akun Google Anda", Toast.LENGTH_SHORT).show()
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
    /** System Alert Dialog **/
    @SuppressLint("ObsoleteSdkInt")
    private fun checkDrawOverlayPermission() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay detected. Please disable any active overlay to proceed.", Toast.LENGTH_SHORT).show()
                // Jika belum ada izin, minta izin overlay
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_CODE)
            } else {
                /*Toast.makeText(this, "Izin overlay sudah diberikan", Toast.LENGTH_SHORT).show()*/
            }
        }
    }

    private fun showLoading() {
        val cardViewProgress = findViewById<CardView>(R.id.card_loading)
        cardViewProgress.visibility = View.VISIBLE // Tampilkan CardView beserta ProgressBar
    }

    // Menyembunyikan `CardView` dan `ProgressBar` setelah proses selesai
   private fun hideLoading() {
        val cardViewProgress = findViewById<CardView>(R.id.card_loading)
        cardViewProgress.visibility = View.GONE // Sembunyikan CardView beserta ProgressBar
    }

    override fun onResume() {
        super.onResume()
      //Cek fingerprint di saat actity di hancurkan
        checkDrawOverlayPermission()
        checkFingerprintStatus()

    }


    companion object {
        private const val REQUEST_CODE = 10101
    }
}
