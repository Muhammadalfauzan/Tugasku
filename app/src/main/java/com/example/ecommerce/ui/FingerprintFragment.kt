package com.example.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.viewModels
import com.example.ecommerce.R
import com.example.ecommerce.ui.auth.LoginViewModel
import com.example.ecommerce.utils.SharedPreferencesUser
import com.example.ecommerce.utils.biometric.BiometricAuthListener
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FingerprintFragment : Fragment(), BiometricAuthListener {

    private lateinit var sharedPrefsUser: SharedPreferencesUser
    private lateinit var fingerprintSwitch: SwitchMaterial
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fingerprint, container, false)

        // Inisialisasi SharedPreferencesUser
        sharedPrefsUser = SharedPreferencesUser(requireContext())

        // Inisialisasi Switch toggle dari layout
        fingerprintSwitch = view.findViewById(R.id.btn_switch)

        // Observasi perubahan status fingerprint dari ViewModel
        loginViewModel.fingerprintStatus.observe(viewLifecycleOwner) { isEnabled ->
            fingerprintSwitch.isChecked = isEnabled  // Set switch sesuai dengan status fingerprint di LiveData
        }

        // Listener untuk switch fingerprint
        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                loginViewModel.updateFingerprintStatus(true)
                Toast.makeText(context, "Fingerprint enabled successfully", Toast.LENGTH_SHORT).show()
            } else {
                loginViewModel.updateFingerprintStatus(false)
                Toast.makeText(context, "Fingerprint disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(context, "Authentication error: $errMsg", Toast.LENGTH_SHORT).show()
        fingerprintSwitch.isChecked = false  // Jika terjadi kesalahan, nonaktifkan switch kembali
        loginViewModel.updateFingerprintStatus(false)  // Perbarui status fingerprint di ViewModel
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        val email = sharedPrefsUser.getUserEmail()
        if (email.isNotEmpty()) {
            // Simpan status fingerprint ke SharedPreferences dan perbarui LiveData di ViewModel
            sharedPrefsUser.saveFingerprintStatus(email, true)
            loginViewModel.updateFingerprintStatus(true)
            Toast.makeText(context, "Fingerprint activated successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}

