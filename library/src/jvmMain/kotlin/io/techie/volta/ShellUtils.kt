/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
