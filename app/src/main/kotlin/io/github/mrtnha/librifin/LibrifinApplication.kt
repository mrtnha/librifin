package io.github.mrtnha.librifin

import android.app.Application
import io.github.mrtnha.librifin.platform.Platform

/** Holds the [AppServices] for the whole process, so they outlive the activity (see [AppServices]). */
class LibrifinApplication : Application() {
    val services by lazy { AppServices(Platform(this)) }
}
