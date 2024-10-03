@file:Suppress("DEPRECATION")

package com.example.ecommerce.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.cardview.widget.CardView
import com.auth0.android.jwt.JWT
import com.example.ecommerce.MainActivity
import com.example.ecommerce.R
import com.example.ecommerce.utils.MyA11yDelegate
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
import java.util.Date

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), BiometricAuthListener {
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPrefsUser: SharedPreferencesUser
    private lateinit var fingerprintButton: MaterialButton
    private lateinit var edEmaail : EditText
    private lateinit var edPassword : EditText
    private lateinit var btLogin : Button

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
        // checkDrawOverlayPermission()

        edEmaail = findViewById(R.id.emailLog_lay)
        edPassword = findViewById(R.id.passLogLay)
        btLogin = findViewById(R.id.bt_login)

        val myDelegate = MyA11yDelegate()
        edEmaail.accessibilityDelegate = myDelegate
        edPassword.accessibilityDelegate = myDelegate
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
        /*}else if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
                // Setel agar overlay disembunyikan hanya jika API level >= 31 (Android 12 ke atas)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.setHideOverlayWindows(true)
                }
            } else {
                Toast.makeText(this, "Overlay permission is required for this app", Toast.LENGTH_SHORT).show()
                finish()
            }*/
        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(this, "Biometric Authentication Error: $errMsg", Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        showLoading()
        val email = sharedPrefsUser.getUserEmail()
        Log.d("FingerprintDebug", "Login attempt with email: $email")

        if (email.isNotEmpty()) {
            // Ambil Google ID Token dari SharedPreferences
            val googleIdToken = sharedPrefsUser.getGoogleIdToken()

            // Cek apakah ID Token sudah kadaluarsa
            if (googleIdToken.isEmpty() || isIdTokenExpired(googleIdToken)) {
                Log.e("FingerprintDebug", "ID Token is expired or missing. Attempting to refresh token.")
                // Lakukan refresh ID token jika sudah kadaluarsa
                refreshGoogleIdTokenAndLogin()
                return
            }

            // Jika ID Token masih valid, lanjutkan login ke Firebase
            if (googleIdToken.isNotEmpty()) {
                Log.d("FingerprintDebug", "Attempting to login with Google ID Token")
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FingerprintDebug", "Login successful with Google account after fingerprint authentication.")
                            sharedPrefsUser.saveLoginStatus(true)  // Set login status ke true
                            hideLoading()
                            navigateToHome()
                        } else {
                            Log.e("FingerprintDebug", "Login failed after fingerprint authentication: ${task.exception?.message}")
                            hideLoading()
                            Toast.makeText(this, "Login gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Log.e("FingerprintDebug", "Google ID Token is missing. Cannot login to Firebase.")
                hideLoading()
                Toast.makeText(this, "Tidak dapat login dengan sidik jari. Silakan login ulang dengan akun Google.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("FingerprintDebug", "Email is missing. Cannot login to Firebase.")
            hideLoading()
            Toast.makeText(this, "Tidak dapat login dengan sidik jari. Silakan login ulang dengan akun Google.", Toast.LENGTH_SHORT).show()
        }
    }

    //fungsi cek token expired
    private fun isIdTokenExpired(idToken: String): Boolean {
        return try {
            val jwt = JWT(idToken)
            val expiresAt = jwt.expiresAt  // Ambil tanggal kadaluarsa token
            expiresAt != null && expiresAt.before(Date())  // Periksa apakah token sudah kadaluarsa
        } catch (e: Exception) {
            e.printStackTrace()
            true  // Jika terjadi error, anggap token sudah kadaluarsa
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
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay terdeteksi. Silakan nonaktifkan overlay untuk melanjutkan.", Toast.LENGTH_SHORT).show()
                // Jika belum ada izin, minta izin overlay
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_CODE)
            } else {
                window.setHideOverlayWindows(true)
            }
        }
    }

    /** Fungsi untuk mengecek apakah google token sudah kadaluarsa **/

    // Fungsi untuk melakukan refresh ID Token Google
    private fun refreshGoogleIdTokenAndLogin() {
        val googleSignInClient = GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // Coba masuk kembali secara diam-diam untuk mendapatkan token baru
        googleSignInClient.silentSignIn().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Dapatkan akun Google yang telah diperbarui
                val googleSignInAccount = task.result
                val newIdToken = googleSignInAccount?.idToken

                if (!newIdToken.isNullOrEmpty()) {
                    // Simpan ID Token baru ke SharedPreferences
                    sharedPrefsUser.saveGoogleIdToken(newIdToken)
                    Log.d("FingerprintDebug", "ID Token refreshed successfully: $newIdToken")

                    // Gunakan ID Token baru untuk login ke Firebase
                    val credential = GoogleAuthProvider.getCredential(newIdToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                Log.d("FingerprintDebug", "Login successful with new ID Token")
                                sharedPrefsUser.saveLoginStatus(true)
                                hideLoading()
                                navigateToHome()
                            } else {
                                Log.e("FingerprintDebug", "Login failed with new ID Token: ${authTask.exception?.message}")
                                hideLoading()
                                Toast.makeText(this, "Login gagal dengan token baru. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("FingerprintDebug", "Failed to refresh ID Token. Token is null or empty.")
                    hideLoading()
                    Toast.makeText(this, "Gagal memperbarui token. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("FingerprintDebug", "Failed to refresh ID Token: ${task.exception?.message}")
                hideLoading()
                Toast.makeText(this, "Gagal memperbarui token. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
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

    @SuppressLint("ObsoleteSdkInt")
    override fun onResume() {
        super.onResume()
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setHideOverlayWindows(true)
        }*/
        checkFingerprintStatus()

    }


    companion object {
        private const val REQUEST_CODE = 10101
        private const val RC_SIGN_IN = 123
    }
}
