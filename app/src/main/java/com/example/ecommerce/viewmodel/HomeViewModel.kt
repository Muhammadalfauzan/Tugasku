package com.example.ecommerce.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecommerce.utils.SharedPreferencesUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesUser  // Dependency injected
) : ViewModel() {

    val userDisplayName = MutableLiveData<String?>()

    init {
        loadUserDisplayName()
    }

    private fun loadUserDisplayName() {
        userDisplayName.value = sharedPreferencesManager.getUserDisplayName()
    }
}

