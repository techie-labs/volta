@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

kotlin {
    androidTarget {
        // Publish both release and debug variants of the Android library
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS Targets
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Kameleoon"
            isStatic = true
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
        }
    }
}

android {
    namespace = "io.techie.kameleoon"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Maven Publish Configuration
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    
    coordinates(
        groupId = "io.techie.kameleoon",
        artifactId = "library",
        version = "1.0.0"
    )

    pom {
        name.set("Kameleoon")
        description.set("An adaptive template for Compose Multiplatform Library")
        inceptionYear.set("2024")
        url.set("https://github.com/yourusername/kameleoon")
        
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        
        developers {
            developer {
                id.set("yourusername")
                name.set("Your Name")
                url.set("https://github.com/yourusername")
            }
        }
        
        scm {
            url.set("https://github.com/yourusername/kameleoon")
            connection.set("scm:git:git://github.com/yourusername/kameleoon.git")
            developerConnection.set("scm:git:ssh://git@github.com/yourusername/kameleoon.git")
        }
    }
}
