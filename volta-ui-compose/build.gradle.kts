@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

version = project.property("VERSION_NAME") as String
group = "io.github.techie-labs"

mavenPublishing {
    coordinates(groupId = "io.github.techie-labs", artifactId = "volta-ui-compose")
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
    pom {
        name.set("Volta UI Compose")
        description.set("Compose Multiplatform UI widgets for the Volta library.")
        inceptionYear.set("2024")
        url.set("https://github.com/fanggadewangga/volta")
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "VoltaUiCompose"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=io.techie.volta.ui.compose")
        }
    }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":library"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.material.icons.extended)
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
    namespace = "io.github.techie_labs.volta.ui.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
