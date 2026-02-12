plugins {
    // Apply plugins to the root project to make them available for subprojects.
    // 'apply false' means the plugin is not applied to the root project itself,
    // but its version is defined here for consistency across the project.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    
    // Plugins applied to the root project
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

// Configuration for Binary Compatibility Validator
apiValidation {
    // Ignore the sample projects as they are not part of the public API
    ignoredProjects.add("sample")
    ignoredProjects.add("shared")
    ignoredProjects.add("androidApp")
    ignoredProjects.add("desktopApp")
}

// Configuration applied to all subprojects (library and sample)
subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    spotless {
        lineEndings = com.diffplug.spotless.LineEnding.UNIX

        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint().editorConfigOverride(mapOf(
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable"
            ))
            // Ensure a consistent copyright header
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
        format("xml") {
            target("**/*.xml")
            targetExclude("**/build/**/*.xml")
        }
    }

    detekt {
        // Use the shared detekt configuration
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}
