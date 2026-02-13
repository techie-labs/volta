@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
    // Apply signing plugin explicitly
    signing
}

// --- DEBUG: CHECK SECRETS PRESENCE ---
println("--- CHECKING GRADLE PROPERTIES (Secrets) ---")
listOf(
    "mavenCentralUsername",
    "mavenCentralPassword",
    "signingInMemoryKey",
    "signingInMemoryKeyId",
    "signingInMemoryKeyPassword",
).forEach { key ->
    if (project.hasProperty(key)) {
        println("✅ Property '$key' is present.")
    } else {
        println("❌ Property '$key' is MISSING.")
    }
}
println("----------------------------------------")
// -------------------------------------

version = project.property("VERSION_NAME") as String
group = "io.techie.volta"

// Maven Publish Configuration
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    // REMOVED: signAllPublications() - We will configure signing manually below
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

// Manual Signing Configuration (Wrapped in afterEvaluate to ensure publications exist)
afterEvaluate {
    signing {
        val key = project.findProperty("signingInMemoryKey") as? String
        val password = project.findProperty("signingInMemoryKeyPassword") as? String
        val keyId = project.findProperty("signingInMemoryKeyId") as? String

        if (!key.isNullOrEmpty() && !password.isNullOrEmpty()) {
            println("🔧 Manually configuring InMemoryPgpKeys...")
            if (!keyId.isNullOrEmpty()) {
                useInMemoryPgpKeys(keyId, key, password)
            } else {
                useInMemoryPgpKeys(key, password)
            }
            
            // Sign all publications created by the plugins
            val publishing = extensions.getByType<PublishingExtension>()
            sign(publishing.publications)
        } else {
            println("⚠️ Signing keys missing or empty. Skipping signing configuration.")
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
        }
    }
}

android {
    namespace = "io.techie.volta"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
