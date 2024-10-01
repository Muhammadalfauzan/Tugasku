package com.example.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    private lateinit var sharedPreferencesManager: SharedPreferencesUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize shared preferences
        sharedPreferencesManager = SharedPreferencesUser(requireContext())

        // Check if user is logged in
        loginViewModel.checkIfUserIsLoggedIn()

        // Observe user data
        loginViewModel.userDisplayName.observe(viewLifecycleOwner) { name ->
            binding.tvProfileName.text = name
        }

        loginViewModel.userPhotoUrl.observe(viewLifecycleOwner) { photoUrl ->
            Glide.with(this).load(photoUrl).into(binding.imgProfile)
        }

        // Handle logout action
        binding.exitButton.setOnClickListener {
            loginViewModel.signOut()
            sharedPreferencesManager.clearUserData()
            navigateToLogin()
        }

        return binding.root
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}

