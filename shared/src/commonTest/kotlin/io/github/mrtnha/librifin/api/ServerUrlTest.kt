package io.github.mrtnha.librifin.api

import kotlin.test.Test
import kotlin.test.assertEquals

class ServerUrlTest {

    @Test
    fun bareHostTriesHttpsThenHttpThenDefaultPort() = assertEquals(
        listOf("https://192.168.1.10", "http://192.168.1.10", "http://192.168.1.10:8096"),
        ServerUrl.candidates("  192.168.1.10/ "),
    )

    @Test
    fun explicitSchemeAndPortIsUsedAsIs() = assertEquals(
        listOf("http://jellyfin.local:8096"),
        ServerUrl.candidates("http://jellyfin.local:8096"),
    )

    @Test
    fun explicitSchemeWithoutPortAlsoTriesDefaultPort() = assertEquals(
        listOf("https://media.example.com", "https://media.example.com:8096"),
        ServerUrl.candidates("HTTPS://media.example.com"),
    )

    @Test
    fun hostWithPortOnlyVariesScheme() = assertEquals(
        listOf("https://nas:8920", "http://nas:8920"),
        ServerUrl.candidates("nas:8920"),
    )

    @Test
    fun subPathIsKept() = assertEquals(
        listOf("https://example.com/jellyfin", "https://example.com:8096/jellyfin"),
        ServerUrl.candidates("https://example.com/jellyfin/"),
    )

    @Test
    fun webClientAndOpdsLinksAreCutToServerRoot() {
        assertEquals(listOf("http://nas:8096"), ServerUrl.candidates("http://nas:8096/web/#/home.html"))
        assertEquals(listOf("http://nas:8096"), ServerUrl.candidates("http://nas:8096/web/index.html"))
        assertEquals(listOf("http://nas:8096/jf"), ServerUrl.candidates("http://nas:8096/jf/opds"))
    }

    @Test
    fun emptyInputHasNoCandidates() {
        assertEquals(emptyList(), ServerUrl.candidates("   "))
        assertEquals(emptyList(), ServerUrl.candidates("https://"))
    }
}
