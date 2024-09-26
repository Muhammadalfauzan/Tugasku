package com.example.ecommerce

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ecommerce.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding :ActivityMainBinding
    @SuppressLint("MissingInflatedId", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
       /* enableEdgeToEdge()*/
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Set icons to dark
            window.statusBarColor = ContextCompat.getColor(this, R.color.white) // Set status bar background to white
        }


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController =navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            when (destination.id) {
                R.id.detailFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
                R.id.searchFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE  // Sembunyikan saat SearchFragment terbuka
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }


}
