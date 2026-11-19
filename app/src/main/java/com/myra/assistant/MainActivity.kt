package com.myra.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        statusText.text = if (allGranted) {
            "Saari basic permissions mil gayi! Myra service start ho rahi hai..."
        } else {
            "Kuch permissions baaki hain. Poora kaam karne ke liye allow karo."
        }
        startMyraService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val grantBtn: Button = findViewById(R.id.grantPermissionsBtn)
        val overlayBtn: Button = findViewById(R.id.overlayBtn)
        val batteryBtn: Button = findViewById(R.id.batteryBtn)

        grantBtn.setOnClickListener {
            requestRequiredPermissions()
        }

        overlayBtn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                statusText.text = "Overlay permission pehle se ON hai."
            }
        }

        batteryBtn.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            statusText.text = "Permissions active hain. Myra background service chal rahi hai."
            startMyraService()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun startMyraService() {
        val intent = Intent(this, MyraForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
