buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

task("make") {
    dependsOn(":app:assembleRelease")
    doLast {
        val buildDir = file("app/build/outputs/apk/release")
        buildDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".apk")) {
                val newName = file.name.replace("-release.apk", ".cs3")
                    .replace("app-", "")
                file.renameTo(file(newName))
            }
        }
    }
}
