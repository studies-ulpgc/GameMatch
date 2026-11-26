plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.sonarqube") version "7.6.0.18749"
}

sonar {
    properties {
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.projectKey", "GameMatch")
        property("sonar.projectName", "GameMatch")
    }
}