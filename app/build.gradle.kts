plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lagradost.cloudstream3.plugins"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3.plugins"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xskip-metadata-version-check"
    }
}

dependencies {
    compileOnly("com.github.recloudstream:cloudstream:master-SNAPSHOT")
    implementation(kotlin("stdlib"))
    implementation("org.jsoup:jsoup:1.15.3")
}
