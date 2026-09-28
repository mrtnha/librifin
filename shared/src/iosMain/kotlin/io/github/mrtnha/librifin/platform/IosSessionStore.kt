package io.github.mrtnha.librifin.platform

/**
 * Stub: keeps the session in memory only, so the login isn't saved across app starts on iOS yet.
 * The real implementation belongs in the Keychain (Security framework), not NSUserDefaults,
 * because the session contains the access token.
 */
internal class IosSessionStore : SessionStore {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun clear() {
        value = null
    }
}
