package io.techie.volta.sample.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.techie.volta.sample.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sample App",
    ) {
        App()
    }
}
