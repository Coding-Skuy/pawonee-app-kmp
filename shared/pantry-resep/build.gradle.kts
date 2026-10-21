plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    `maven-publish`
}

// Artefak terkunci: id.chefgenie.pawonee:pantry-resep:1.0.0 (AGENTS.md; PWN-KMP-002).
group = "id.chefgenie.pawonee"
version = "1.0.0"

kotlin {
    explicitApi()
    jvmToolchain(17)

    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Hanya stdlib + serialization: modul ini dipakai Pedaree sebagai konsumen.
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "id.chefgenie.pawonee.pantryresep"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    repositories {
        // Repositori lokal untuk verifikasi artefak; registri resmi diputuskan di tiket rilis.
        maven {
            name = "build"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}
