@file:OptIn(ExperimentalReadiumApi::class)

package io.github.mrtnha.librifin.ui.reader

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentFactory
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import org.json.JSONException
import org.json.JSONObject
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.navigator.util.DirectionalNavigationAdapter
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.isRestricted
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

@Composable
actual fun EpubView(
    file: Path,
    initialLocator: String?,
    onPositionChanged: (locator: String, progress: Double) -> Unit,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier,
) {
    val application = LocalContext.current.applicationContext as Application
    val vm = viewModel { EpubViewModel(application, File(file.toString()), initialLocator) }

    when (val state = vm.state) {
        EpubState.Opening -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EpubState.Failed -> LaunchedEffect(state) { onOpenFailed(state.message) }
        EpubState.Opened -> {
            val currentOnCenterTap by rememberUpdatedState(onCenterTap)
            val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
            // Created by ReaderFragmentFactory, from the book the view model opened.
            AndroidFragment<EpubNavigatorFragment>(modifier) { navigator ->
                // Taps near the left and right edges turn the page with a slide, like a swipe.
                // Taps elsewhere reach the next listener.
                navigator.addInputListener(DirectionalNavigationAdapter(navigator, animatedTransition = true))
                navigator.addInputListener(object : InputListener {
                    override fun onTap(event: TapEvent): Boolean {
                        currentOnCenterTap()
                        return true
                    }
                })
                navigator.lifecycleScope.launch {
                    navigator.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        navigator.currentLocator.collect { locator ->
                            currentOnPositionChanged(
                                locator.toJSON().toString(),
                                locator.locations.totalProgression ?: 0.0,
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed interface EpubState {
    data object Opening : EpubState
    data object Opened : EpubState
    class Failed(val message: String) : EpubState
}

/**
 * Opens the book with Readium and keeps it open while the reader is on the back stack
 * (so it survives rotation). The publication is closed when the reader is left.
 */
private class EpubViewModel(
    private val application: Application,
    private val file: File,
    private val initialLocator: String?,
) : ViewModel() {
    var state by mutableStateOf<EpubState>(EpubState.Opening)
        private set

    private var publication: Publication? = null
    private var fragmentFactory: FragmentFactory? = null

    init {
        viewModelScope.launch { state = open() }
    }

    private suspend fun open(): EpubState {
        val httpClient = DefaultHttpClient()
        val assetRetriever = AssetRetriever(application.contentResolver, httpClient)
        val opener = PublicationOpener(
            DefaultPublicationParser(application, httpClient, assetRetriever, pdfFactory = null),
        )

        val asset = assetRetriever.retrieve(file).getOrElse { return EpubState.Failed(NOT_AN_EPUB) }
        val publication = opener.open(asset, allowUserInteraction = false).getOrElse {
            asset.close()
            return EpubState.Failed(NOT_AN_EPUB)
        }
        this.publication = publication

        when {
            !publication.conformsTo(Publication.Profile.EPUB) -> return EpubState.Failed(NOT_AN_EPUB)
            publication.isRestricted -> return EpubState.Failed("This book is protected (DRM) and can't be opened.")
        }

        val factory = EpubNavigatorFactory(publication).createFragmentFactory(
            initialLocator = initialLocator?.let { json ->
                try {
                    Locator.fromJSON(JSONObject(json))
                } catch (_: JSONException) {
                    null
                }
            },
        )
        fragmentFactory = factory
        ReaderFragmentFactory.epub = factory
        return EpubState.Opened
    }

    override fun onCleared() {
        if (ReaderFragmentFactory.epub === fragmentFactory) ReaderFragmentFactory.epub = null
        publication?.close()
    }

    private companion object {
        const val NOT_AN_EPUB = "This book can't be opened. Only EPUB files are supported."
    }
}
