package io.github.mrtnha.librifin.ui.licenses

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import io.github.mrtnha.librifin.platform.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LicensesViewModel(platform: Platform) : ViewModel() {
    /** Sorted by name. Null while loading, empty if the platform has no list. */
    var libraries by mutableStateOf<List<Library>?>(null)
        private set

    /**
     * Kept here rather than in the screen, which is left while a license is open: coming back, the list is
     * still where it was.
     */
    val listState = LazyListState()

    init {
        viewModelScope.launch {
            libraries = withContext(Dispatchers.IO) {
                platform.readLicensesJson()?.let { Libs.Builder().withJson(it).build().libraries }.orEmpty()
            }
        }
    }
}
