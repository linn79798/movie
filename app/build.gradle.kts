plugins {
      id("com.android.application")
      id("kotlin-android")
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
          sourceCompatibility = JavaVersion.VERSION_1_8
          targetCompatibility = JavaVersion.VERSION_1_8
      }

      kotlinOptions {
          jvmTarget = "1.8"
      }
  }

  dependencies {

  compileOnly("com.github.recloudstream:cloudstream:pre-release")
      implementation(kotlin("stdlib"))
      implementation("org.jsoup:jsoup:1.13.1")
  }
