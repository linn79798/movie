plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
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
