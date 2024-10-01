package com.example.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentProfileBinding
import com.example.ecommerce.ui.auth.LoginActivity
import com.example.ecommerce.ui.auth.LoginViewModel
import com.example.ecommerce.utils.SharedPreferencesUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var sharedPrefsUser: SharedPreferencesUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan ViewBinding
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize SharedPreferences
        sharedPrefsUser = SharedPreferencesUser(requireContext())

        // Cek apakah user sudah login atau belum
        loginViewModel.checkIfUserIsLoggedIn()

        // Observe data user untuk menampilkan profil
        loginViewModel.userDisplayName.observe(viewLifecycleOwner) { name ->
            binding.tvProfileName.text = name
        }

        loginViewModel.userPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            Glide.with(this).load(photoUrl).into(binding.imgProfile)
        }

        // Handle logout action
        binding.exitButton.setOnClickListener {
            performLogout() // Panggil fungsi logout ketika tombol exit ditekan
        }

        // Handle navigasi ke FingerprintFragment
        binding.notification.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_fingerprintFragment)
        }

        return binding.root
    }

    private fun performLogout() {
        Log.d("ProfileFragment", "Logout button clicked, logging out user")

        // Panggil fungsi logout di ViewModel
        loginViewModel.logoutUser()

        // Navigasi ke halaman login setelah logout
        navigateToLogin()
    }


    // Fungsi untuk navigasi ke LoginActivity setelah logout
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Menutup Activity saat ini
    }
}


