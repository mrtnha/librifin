package io.github.mrtnha.librifin.ui.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.DiscoveryResponse
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.api.ServerUrl
import io.github.mrtnha.librifin.api.decodeOrNull
import io.github.mrtnha.librifin.platform.ServerDiscovery
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ServerSelectionViewModel(
    private val jellyfin: JellyfinClient,
    private val discovery: ServerDiscovery,
) : ViewModel() {

    // Manual entry
    var urlInput by mutableStateOf("")
        private set
    var manualServer by mutableStateOf<Server?>(null)
        private set
    var isTestingUrl by mutableStateOf(false)
        private set
    var urlError by mutableStateOf<String?>(null)
        private set

    // Discovery
    val discoveredServers = mutableStateListOf<Server>()

    /** Set when discovery has run for a while without finding anything. */
    var isNothingFound by mutableStateOf(false)
        private set

    /** Discovered servers come first; manual entry is opened on request. */
    var isManualEntryVisible by mutableStateOf(false)
        private set

    fun showManualEntry() {
        isManualEntryVisible = true
    }
    private val verifyingIds = mutableSetOf<String>()

    private var testJob: Job? = null
    private var discoveryJob: Job? = null

    /** Like Finamp: test the typed URL automatically once the user stops typing for a moment. */
    fun onUrlChanged(text: String) {
        urlInput = text
        manualServer = null
        urlError = null
        testJob?.cancel()
        if (text.isBlank()) {
            isTestingUrl = false
            return
        }
        testJob = viewModelScope.launch {
            delay(URL_TEST_DEBOUNCE_MS)
            isTestingUrl = true
            try {
                manualServer = findServer(text)
                if (manualServer == null) urlError = "No Jellyfin server found at this address."
            } finally {
                isTestingUrl = false
            }
        }
    }

    /** Tries all URL variants in order and returns the first that answers like a Jellyfin server. */
    private suspend fun findServer(input: String): Server? {
        for (url in ServerUrl.candidates(input)) {
            val server = verify(url, discoveryName = null)
            if (server != null) return server
        }
        return null
    }

    /** Runs while the screen is visible, see [ServerSelectionScreen]. */
    fun startDiscovery() {
        if (discoveryJob?.isActive == true) return
        discoveryJob = viewModelScope.launch {
            launch {
                delay(NOTHING_FOUND_AFTER_MS)
                isNothingFound = discoveredServers.isEmpty()
            }
            discovery.replies().collect { text ->
                val reply = JellyfinClient.json.decodeOrNull<DiscoveryResponse>(text) ?: return@collect
                val id = reply.id ?: return@collect
                val address = reply.address?.trimEnd('/') ?: return@collect
                // Servers answer every broadcast; only check each one once at a time.
                if (discoveredServers.any { it.id == id } || !verifyingIds.add(id)) return@collect
                launch {
                    try {
                        val server = verify(address, reply.name)
                        if (server != null && discoveredServers.none { it.id == server.id }) {
                            discoveredServers += server
                        }
                    } finally {
                        verifyingIds.remove(id)
                    }
                }
            }
        }
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        verifyingIds.clear()
    }

    /** Asks `/System/Info/Public`; null if the URL isn't a reachable Jellyfin server. */
    private suspend fun verify(baseUrl: String, discoveryName: String?): Server? {
        val info = try {
            jellyfin.publicSystemInfo(baseUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return null // unreachable, wrong address, or not Jellyfin
        }
        val id = info.id ?: return null
        val serverName = info.serverName
        val name = when {
            serverName.isNullOrBlank() -> discoveryName ?: baseUrl
            discoveryName.isNullOrBlank() || discoveryName == serverName -> serverName
            else -> "$serverName ($discoveryName)"
        }
        return Server(baseUrl = baseUrl, id = id, name = name, version = info.version)
    }

    private companion object {
        const val URL_TEST_DEBOUNCE_MS = 500L
        const val NOTHING_FOUND_AFTER_MS = 6_000L
    }
}
