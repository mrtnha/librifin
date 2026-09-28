package io.github.mrtnha.librifin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform