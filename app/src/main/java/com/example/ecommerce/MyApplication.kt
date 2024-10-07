package com.example.ecommerce

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.aheaditec.talsec_security.security.api.SuspiciousAppInfo
import com.aheaditec.talsec_security.security.api.Talsec
import com.aheaditec.talsec_security.security.api.TalsecConfig
import com.aheaditec.talsec_security.security.api.ThreatListener
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application(), ThreatListener.ThreatDetected{

    companion object {
        private const val expectedPackageName = "com.example.ecommerce"
        private val expectedSigningCertificateHashBase64 = arrayOf(
            "MH3NpFYxxZtkCFFFtE/IhYgpIqzgHJpYUUy+AZeW9C8=",
            "wuIbSWatdsmlZlScyYD6z3OYceoF1Il5hoatYDIvLFE="

        )
        private const val watcherMail = "john@example.com"
        private val supportedAlternativeStores = arrayOf(
            "com.sec.android.app.samsungapps"
        )
        private val isProd = true
    }

    override fun onCreate() {
        super.onCreate()

        val config = TalsecConfig.Builder(
            expectedPackageName,
            expectedSigningCertificateHashBase64)
            .watcherMail(watcherMail)
            .supportedAlternativeStores(supportedAlternativeStores)
            .prod(isProd)
            .build()

        ThreatListener(this).registerListener(this)
        Talsec.start(this,config)

    }
    // Implementasi Listener untuk Ancaman yang Terdeteksi
    override fun onRootDetected() {
        Log.e("ThreatListener", "Root Detected!")
        // showToast("Root Access Detected! The app might be at risk.")
    }

    override fun onDebuggerDetected() {
        Log.e("ThreatListener", "Debugger Detected!")
       // showToast("Debugger Detected! Please remove it to continue.")
    }

    override fun onEmulatorDetected() {
        Log.e("ThreatListener", "Emulator Detected!")
       // showToast("Emulator Environment Detected!")
    }

    override fun onTamperDetected() {
        Log.e("ThreatListener", "Tampering Detected!")
       /* showToast("The app has been tampered with. It might not be secure.")*/
    }

    override fun onUntrustedInstallationSourceDetected() {
        Log.e("ThreatListener", "Untrusted Installation Source Detected!")
       /* showToast("The app was installed from an untrusted source.")*/
    }

    override fun onHookDetected() {
        Log.e("ThreatListener", "Hook Detected!")
       /* showToast("Hooking framework detected! The app might be compromised.")*/
    }

    override fun onDeviceBindingDetected() {
        Log.e("ThreatListener", "Device Binding Detected!")
       /* showToast("Device binding security violation detected.")*/
    }

    override fun onObfuscationIssuesDetected() {
        Log.e("ThreatListener", "Obfuscation Issues Detected!")
        /*showToast("Obfuscation issues detected! The app might be at risk.")*/
    }

    override fun onMalwareDetected(maliciousApps: MutableList<SuspiciousAppInfo>?) {
        Log.e("ThreatListener", "Malware Detected!")
       /* showToast("Malware Detected! Potential malicious apps installed.")*/
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}