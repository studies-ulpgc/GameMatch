import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.sonarqube") version "7.6.0.18749"
}

val sonarPropertiesFile = rootProject.file("sonar-project.properties")
if (sonarPropertiesFile.exists()) {
    val sonarProps = Properties().apply {
        sonarPropertiesFile.inputStream().use { load(it) }
    }
    sonar {
        properties {
            sonarProps.forEach { (key, value) ->
                val propKey = key.toString()
                if (propKey != "sonar.token") {
                    property(propKey, value.toString())
                }
            }
        }
    }
}
