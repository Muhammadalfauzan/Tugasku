package com.example.ecommerce

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ecommerce.databinding.ActivityMainBinding
import com.example.ecommerce.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val cartViewModel: CartViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window: Window = window
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.detailFragment, R.id.searchFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }

        }
        observeCartItemCount()
    }

    // badge Menambahkan badge pada item cartFragment
    private fun observeCartItemCount() {
        cartViewModel.cartItemCount.observe(this) { itemCount ->
            // Logging untuk memastikan data terupdate dengan benar
            Log.d("MainActivity", "Cart item count observed: $itemCount")

            // Dapatkan BadgeDrawable untuk item cartFragment
            val badge = binding.bottomNavigationView.getOrCreateBadge(R.id.cartFragment)

            // Atur visibilitas dan nomor badge sesuai dengan jumlah item di keranjang
            if (itemCount > 0) {
                Log.d("MainActivity", "Badge visible with count: $itemCount")
                badge.isVisible = true
                badge.number = itemCount
            } else {
                Log.d("MainActivity", "Badge hidden because count is zero.")
                badge.isVisible = false
            }
        }
    }
}

        /*      // Atur back button behavior untuk HomeFragment
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    // Log untuk memastikan callback dieksekusi
                    Log.d("MainActivity", "Back pressed in HomeFragment - Exiting app")
                    finish() // Keluar dari aplikasi jika berada di HomeFragment
                } else {
                    navController.popBackStack() // Kembali ke fragment sebelumnya jika tidak di HomeFragment
                }
            }
        })*/

        /*  // Set up listener untuk BottomNavigationView agar menghindari reload fragment yang sudah ada
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.cartFragment -> {
                    if (navController.currentDestination?.id != R.id.cartFragment) {
                        navController.navigate(R.id.cartFragment)
                    }
                    true
                }
                R.id.profileFragment -> {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }*/




