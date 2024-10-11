package com.example.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.ecommerce.databinding.FragmentProfileBinding
import com.example.ecommerce.ui.auth.LoginActivity
import com.example.ecommerce.ui.auth.LoginViewModel
import com.example.ecommerce.utils.SharedPreferencesUser
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
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // inisialisasi SharedPreferences
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

        binding.exitButton.setOnClickListener {
            performLogout()
        }

        // Handle navigasi ke FingerprintFragment
        binding.notification.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFingerprintFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun performLogout() {
        Log.d("ProfileFragment", "Logout button clicked, logging out user")

        loginViewModel.logoutUser()

        navigateToLogin()
    }



    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
        // Mencegah screenshot di ProfileFragment
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun onPause() {
        super.onPause()
        // Mengizinkan screenshot di fragment lain
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}


