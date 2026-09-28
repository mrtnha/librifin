package io.github.mrtnha.librifin

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import io.github.mrtnha.librifin.platform.AndroidPlatform

class MainActivity : ComponentActivity() {
    private val requestLocalNetwork = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Nothing to do: without it, discovery finds nothing and LAN addresses fail with a clear error.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestLocalNetworkAccess()

        val platform = AndroidPlatform(applicationContext)
        setContent {
            App(platform)
        }
    }

    /** Android 17+ requires a runtime permission to talk to devices on the local network (the Jellyfin server). */
    private fun requestLocalNetworkAccess() {
        if (Build.VERSION.SDK_INT < 37) return
        val permission = "android.permission.ACCESS_LOCAL_NETWORK"
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestLocalNetwork.launch(permission)
        }
    }
}
