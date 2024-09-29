package com.example.ecommerce.utils.biometric

interface BiometricAuthListener {
    fun onBiometricAuthenticateError(error: Int, errMsg: String)
    fun onBiometricAuthenticateSuccess(result: androidx.biometric.BiometricPrompt.AuthenticationResult)
}