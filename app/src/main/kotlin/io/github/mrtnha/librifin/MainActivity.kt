package io.github.mrtnha.librifin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import io.github.mrtnha.librifin.ui.reader.ReaderFragmentFactory

// A FragmentActivity because Readium's EPUB renderer is a Fragment.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = ReaderFragmentFactory
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ReaderFragmentFactory.removeOrphans(supportFragmentManager)

        val appContainer = (application as LibrifinApplication).appContainer
        setContent {
            App(appContainer)
        }
    }
}
