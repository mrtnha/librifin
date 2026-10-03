package io.github.mrtnha.librifin

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import io.github.mrtnha.librifin.ui.reader.ReaderFragmentFactory

// A FragmentActivity because Readium's EPUB renderer is a Fragment.
class MainActivity : FragmentActivity() {
    private val requestLocalNetwork = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Nothing to do: without it, discovery finds nothing and LAN addresses fail with a clear error.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = ReaderFragmentFactory
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ReaderFragmentFactory.removeOrphans(supportFragmentManager)
        requestLocalNetworkAccess()

        val services = (application as LibrifinApplication).services
        setContent {
            App(services)
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
