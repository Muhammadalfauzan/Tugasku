package com.example.ecommerce.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.ecommerce.utils.SharedPreferencesUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val sharedPreferencesManager = SharedPreferencesUser(getApplication<Application>())

    val userDisplayName = MutableLiveData<String>()
    val userEmail = MutableLiveData<String>()

    // Fungsi untuk memuat data user dari SharedPreferences
    fun loadUserData() {
        userDisplayName.value = sharedPreferencesManager.getUserDisplayName()
        userEmail.value = sharedPreferencesManager.getUserEmail()
    }
}