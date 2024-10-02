package com.example.ecommerce

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ecommerce.ui.auth.LoginActivity
import com.example.ecommerce.ui.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private val splashTimeOut: Long = 4500
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            // Pindah ke activity login setelah SPLASH_TIME_OUT
            observeLoginState()
            finish()
        }, splashTimeOut)
    }
    private fun observeLoginState() {
        loginViewModel.authState.observe(this) { isLoggedIn ->
            if (isLoggedIn == true) {
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }else {
                startActivity(Intent(this,LoginActivity::class.java))
                finish()
            }
        }
    }
}