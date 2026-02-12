package io.techie.volta

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Internal utility for executing system commands and reading files.
 */
internal object ShellUtils {

    fun execute(command: String, timeoutSeconds: Long = 5): String {
        return try {
            val parts = command.split(" ")
            val process = ProcessBuilder(parts)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            output.trim()
        } catch (_: Exception) {
            ""
        }
    }

    fun readFile(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun parseValue(output: String, key: String): String? {
        return Regex("$key=(\\d+|\\w+)").find(output)?.groupValues?.get(1)
    }
}
