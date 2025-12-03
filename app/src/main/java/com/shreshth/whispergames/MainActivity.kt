package com.shreshth.whispergames

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.whispergames.app.ui.HomeActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MICROPHONE_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            navigateToHome()
        } else {
            requestMicrophonePermission()
        }
    }

    private fun requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(this)
                .setTitle("Microphone Permission Required")
                .setMessage("This app requires microphone access to play sound-controlled games. Please grant the permission to continue.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        MICROPHONE_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("Cancel") { _, _ -> finish() }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MICROPHONE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == MICROPHONE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigateToHome()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Microphone permission is required to play Whisper Games. The app will now close.")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
