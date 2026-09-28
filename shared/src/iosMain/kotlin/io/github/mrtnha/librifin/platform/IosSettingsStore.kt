package io.github.mrtnha.librifin.platform

/** Stub: keeps the settings in memory only, so they aren't saved across app starts on iOS yet. */
internal class IosSettingsStore : SettingsStore {
    private val values = mutableMapOf<String, String>()

    override fun read(key: String): String? = values[key]

    override fun write(key: String, value: String) {
        values[key] = value
    }
}
