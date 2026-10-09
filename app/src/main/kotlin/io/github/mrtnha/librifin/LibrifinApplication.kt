package io.github.mrtnha.librifin

import android.app.Application

/** Holds the [AppContainer] for the whole process, so it outlives the activity (see [AppContainer]). */
class LibrifinApplication : Application() {
    val appContainer by lazy { AppContainer(this) }
}
