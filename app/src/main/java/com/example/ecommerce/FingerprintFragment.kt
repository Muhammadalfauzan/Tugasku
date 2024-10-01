package com.example.ecommerce

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.ui.auth.LoginViewModel
import com.example.ecommerce.utils.SharedPreferencesUser
import com.example.ecommerce.utils.biometric.BiometricAuthListener
import com.example.ecommerce.utils.biometric.BiometricUtils
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FingerprintFragment : Fragment(), BiometricAuthListener {

    private lateinit var sharedPrefsUser: SharedPreferencesUser
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var fingerprintSwitch: SwitchMaterial
    private val loginViewModel: LoginViewModel by viewModels()
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fingerprint, container, false)

        // Inisialisasi SharedPreferencesUser untuk menyimpan status fingerprint
        sharedPrefsUser = SharedPreferencesUser(requireContext())

        // Inisialisasi Switch toggle dari layout
        fingerprintSwitch = view.findViewById(R.id.btn_switch)

        // Mengambil UID pengguna saat ini (gunakan UID demo jika tidak terhubung dengan Firebase)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Setel switch berdasarkan status fingerprint yang disimpan di SharedPreferences
        fingerprintSwitch.isChecked = sharedPrefsUser.getFingerprintStatus(uid)

        loginViewModel.fingerprintStatus.observe(viewLifecycleOwner) { isEnabled ->
            fingerprintSwitch.isChecked = isEnabled
        }
        // Listener untuk switch fingerprint
        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                loginViewModel.updateFingerprintStatus(isChecked)
                // Jika switch diaktifkan, tampilkan prompt biometric authentication
                if (BiometricUtils.isBiometricReady(requireContext())) {
                    BiometricUtils.showBiometricPrompt(requireActivity() as AppCompatActivity, this)
                } else {
                    // Jika perangkat tidak mendukung biometric, beri tahu pengguna dan nonaktifkan switch
                    Toast.makeText(context, "Biometric not supported or enabled on this device", Toast.LENGTH_SHORT).show()
                    fingerprintSwitch.isChecked = false
                }
            } else {
                // Jika switch dinonaktifkan, simpan status fingerprint ke SharedPreferences
                sharedPrefsUser.saveFingerprintStatus(uid, false)
                Toast.makeText(context, "Fingerprint disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Implementasi callback BiometricAuthListener
    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Toast.makeText(context, "Authentication error: $errMsg", Toast.LENGTH_SHORT).show()
        // Jika terjadi kesalahan, nonaktifkan switch kembali
        fingerprintSwitch.isChecked = false
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        val email = sharedPrefsUser.getUserEmail() // Gunakan email untuk menyimpan status fingerprint
        sharedPrefsUser.saveFingerprintStatus(email, true)
        Log.d("FingerprintDebug", "Fingerprint activated and saved successfully for email: $email")

        Toast.makeText(context, "Fingerprint activated successfully!", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_fingerprintFragment_to_profileFragment)
    }
}
