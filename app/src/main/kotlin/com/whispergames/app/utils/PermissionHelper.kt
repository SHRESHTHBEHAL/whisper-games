package com.whispergames.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing microphone permissions
 */
object PermissionHelper {

    const val MICROPHONE_PERMISSION_REQUEST_CODE = 100

    /**
     * Check if microphone permission is granted
     */
    fun isMicrophonePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request microphone permission
     */
    fun requestMicrophonePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            MICROPHONE_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Check if we should show permission rationale
     */
    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
