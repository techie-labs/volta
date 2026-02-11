package io.techie.volta

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
