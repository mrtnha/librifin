package io.github.mrtnha.librifin.storage

import android.content.SharedPreferences

/** Stores one serialized session. */
class SessionStore(private val prefs: SharedPreferences) {
    fun read(): String? = prefs.getString(KEY_SESSION, null)
    fun write(value: String) = prefs.edit().putString(KEY_SESSION, value).apply()
    fun clear() = prefs.edit().remove(KEY_SESSION).apply()

    private companion object {
        const val KEY_SESSION = "session"
    }
}

/** Stores small settings as text under a key. Nothing secret goes here. */
class SettingsStore(private val prefs: SharedPreferences) {
    fun read(key: String): String? = prefs.getString(key, null)
    fun write(key: String, value: String) = prefs.edit().putString(key, value).apply()
}
