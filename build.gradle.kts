buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
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
