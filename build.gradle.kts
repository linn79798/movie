plugins {
    id("com.android.application") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

task("make") {
    dependsOn(":app:assembleRelease")
    doLast {
        val buildDir = file("app/build/outputs/apk/release")
        println("Looking for APK in: " + buildDir.absolutePath)
        val apkFile = buildDir.listFiles()?.firstOrNull { it.name.endsWith(".apk") }

        if (apkFile != null) {
            println("Found APK: " + apkFile.name)
            val destFile = file("Hoathinh3DProvider.cs3")
            apkFile.copyTo(destFile, overwrite = true)
            println("Copied and renamed to: " + destFile.absolutePath)
        } else {
            throw GradleException("No APK file found in " + buildDir.absolutePath)
        }
    }
}
