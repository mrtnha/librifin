package io.github.mrtnha.librifin

import android.app.Application

/** Holds the [AppServices] for the whole process, so they outlive the activity (see [AppServices]). */
class LibrifinApplication : Application() {
    val services by lazy { AppServices(this) }
}
