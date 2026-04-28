@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

version = project.property("VERSION_NAME") as String
group = "io.github.techie-labs"

mavenPublishing {
    coordinates(groupId = "io.github.techie-labs", artifactId = "volta-mock")
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
    pom {
        name.set("Volta Mock")
        description.set("Mocking tools for Volta library, useful for Compose Previews and testing.")
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
            baseName = "VoltaMock"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=io.techie.volta.mock")
        }
    }
    jvm()
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":library"))
            implementation(libs.kotlinx.coroutines.test)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.github.techie_labs.volta.mock"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
