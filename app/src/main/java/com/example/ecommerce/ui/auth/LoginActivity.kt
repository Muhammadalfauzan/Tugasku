package com.example.ecommerce.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecommerce.MainActivity
import com.example.ecommerce.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123  // Request code untuk Google Sign-In

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // Ganti dengan layout yang sesuai

        // Konfigurasi Google Sign-In Options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup tombol Google Sign-In
        findViewById<SignInButton>(R.id.google_signIn).setOnClickListener {
            signInWithGoogle()
        }

        // Observe perubahan status login dari ViewModel
        loginViewModel.authState.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToProfile()
            } else {
                // Tampilkan pesan error jika gagal
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk memulai proses Google Sign-In
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Menangani hasil dari Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                loginViewModel.firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Tangani error Google Sign-In
                e.printStackTrace()
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Navigasi ke halaman profil setelah login berhasil
    private fun navigateToProfile() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Hentikan LoginActivity agar tidak bisa kembali
    }
}
