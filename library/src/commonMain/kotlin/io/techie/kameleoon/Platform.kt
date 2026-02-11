package io.techie.kameleoon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
