@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

version = project.property("VERSION_NAME") as String
group = "io.github.techie-labs"

// Maven Publish Configuration
mavenPublishing {
    coordinates(
        groupId = "io.github.techie-labs",
        artifactId = "volta",
    )
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

    pom {
        name.set("Volta")
        description.set(
            "Volta ⚡ — Kotlin Multiplatform Battery Library. " +
                "Monitor battery health, charging status, and advanced diagnostics across Android, iOS, and Desktop with unified API.",
        )
        inceptionYear.set("2024")
        url.set("https://github.com/fanggadewangga/volta")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("fanggadewangga")
                name.set("Fangga Dewangga")
                url.set("https://github.com/fanggadewangga")
            }
        }

        scm {
            url.set("https://github.com/fanggadewangga/volta")
            connection.set("scm:git:git://github.com/fanggadewangga/volta.git")
            developerConnection.set("scm:git:ssh://git@github.com/fanggadewangga/volta.git")
        }
    }
}

kotlin {
    androidTarget {
        // Publish only release variant
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS Targets
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Volta"
            isStatic = true
            // Explicitly set bundle ID to avoid warnings and potential build issues
            freeCompilerArgs += listOf("-Xbinary=bundleId=io.techie.volta")
        }
    }

    // Desktop (JVM) Target
    jvm()

    // Web (Wasm) Target
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "io.github.fanggadewangga.volta"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
