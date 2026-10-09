package io.github.mrtnha.librifin.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewOutcomeReceiver
import androidx.webkit.WebViewStartUpConfig
import androidx.webkit.WebViewStartUpResult
import androidx.webkit.WebViewStartupException
import java.util.concurrent.Executor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

/**
 * Gets the book renderer ready ahead of time while this is in the composition, so the first book
 * after an app start opens faster. Only the first call per app start does anything.
 *
 * Starts WebView's engine, which Readium shows the books in. Its first start in an app process
 * takes 0.15–0.25 s; started here, in the background, it's ready before a book is tapped.
 */
@Composable
fun PrepareReader() {
    val context = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        if (isWebViewStarted) return@LaunchedEffect
        isWebViewStarted = true
        WebViewCompat.startUpWebView(
            context,
            WebViewStartUpConfig.Builder(startUpExecutor).build(),
            WebViewOutcomeReceiver<WebViewStartUpResult, WebViewStartupException> {
                // Nothing to do: the reader's first WebView finds the engine running.
            },
        )
    }
}

/** The engine stays loaded until the app process ends. Main thread only. */
private var isWebViewStarted = false

/**
 * Runs WebView's background start-up work on the IO threads. If WebView can't start (e.g. it's
 * disabled), that mustn't crash the library; opening a book then behaves as without the early start.
 */
private val startUpExecutor = Executor { task ->
    Dispatchers.IO.asExecutor().execute {
        try {
            task.run()
        } catch (_: Exception) {
        }
    }
}
