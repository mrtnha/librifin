package io.github.mrtnha.librifin.api

/**
 * Turns what the user typed into the URLs worth trying, in order:
 * - no scheme → `https://`, then `http://`
 * - no port → additionally Jellyfin's default port 8096
 * Pasted web client or OPDS links (`…/web/#/home`, `…/opds`) are cut back to the server root,
 * a reverse-proxy sub-path (`https://host/jellyfin`) is kept.
 */
object ServerUrl {
    const val DEFAULT_PORT = 8096

    fun candidates(input: String): List<String> {
        val url = input.trim().substringBefore('#').substringBefore('?')
        val scheme = SCHEME.find(url)?.value?.lowercase()
        val rest = (if (scheme != null) url.substring(scheme.length) else url)
            .replace(TRAILING_CLIENT_PATH, "")
            .trimEnd('/')
        if (rest.isEmpty()) return emptyList()

        val authority = rest.substringBefore('/')
        val path = rest.substring(authority.length)
        val hasPort = PORT.containsMatchIn(authority)

        val schemes = if (scheme != null) listOf(scheme) else listOf("https://", "http://")
        val result = schemes.map { it + authority + path }.toMutableList()
        if (!hasPort) result += schemes.last() + authority + ":$DEFAULT_PORT" + path
        return result.distinct()
    }

    private val SCHEME = Regex("^https?://", RegexOption.IGNORE_CASE)
    private val PORT = Regex(":\\d+$")
    private val TRAILING_CLIENT_PATH = Regex("/(web|opds)(/.*)?$", RegexOption.IGNORE_CASE)
}
