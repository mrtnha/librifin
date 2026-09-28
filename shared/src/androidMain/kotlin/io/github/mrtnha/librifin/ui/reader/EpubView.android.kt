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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import org.readium.navigator.common.InputListener
import org.readium.navigator.common.TapContext
import org.readium.navigator.common.TapEvent
import org.readium.navigator.common.defaultInputListener
import org.readium.navigator.web.reflowable.ReflowableWebRendition
import org.readium.navigator.web.reflowable.ReflowableWebRenditionFactory
import org.readium.navigator.web.reflowable.ReflowableWebRenditionState
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Layout
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

@Composable
actual fun EpubView(
    file: Path,
    onCenterTap: () -> Unit,
    onOpenFailed: (message: String) -> Unit,
    modifier: Modifier,
) {
    val application = LocalContext.current.applicationContext as Application
    val vm = viewModel { EpubViewModel(application, File(file.toString())) }

    when (val state = vm.state) {
        EpubState.Opening -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is EpubState.Failed -> LaunchedEffect(state) { onOpenFailed(state.message) }
        is EpubState.Opened -> {
            val currentOnCenterTap by rememberUpdatedState(onCenterTap)
            // Readium turns the page on taps near the left and right edges and passes all other taps on.
            val centerTapListener = remember {
                object : InputListener {
                    override fun onTap(event: TapEvent, context: TapContext) = currentOnCenterTap()
                }
            }
            ReflowableWebRendition(
                state = state.rendition,
                modifier = modifier,
                inputListener = defaultInputListener(
                    controller = state.rendition.controller,
                    fallbackListener = centerTapListener,
                ),
            )
        }
    }
}

private sealed interface EpubState {
    data object Opening : EpubState
    class Opened(val rendition: ReflowableWebRenditionState) : EpubState
    class Failed(val message: String) : EpubState
}

/**
 * Opens the book with Readium and keeps it open while the reader is on the back stack
 * (so it survives rotation). The publication is closed when the reader is left.
 */
private class EpubViewModel(private val application: Application, private val file: File) : ViewModel() {
    var state by mutableStateOf<EpubState>(EpubState.Opening)
        private set

    private var publication: Publication? = null

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

        val factory = ReflowableWebRenditionFactory(application, publication)
            ?: return EpubState.Failed(
                when {
                    !publication.conformsTo(Publication.Profile.EPUB) -> NOT_AN_EPUB
                    publication.metadata.layout == Layout.FIXED -> "Fixed-layout EPUBs aren't supported yet."
                    else -> "This book can't be opened. It may be protected (DRM)."
                },
            )
        val rendition = factory.createRenditionState()
            .getOrElse { return EpubState.Failed("This book can't be opened.") }
        return EpubState.Opened(rendition)
    }

    override fun onCleared() {
        publication?.close()
    }

    private companion object {
        const val NOT_AN_EPUB = "This book can't be opened. Only EPUB files are supported."
    }
}
