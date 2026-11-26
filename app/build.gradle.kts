import java.util.Properties
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("org.sonarqube")
    jacoco
}

android {
    namespace = "com.pamn.gamematch"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pamn.gamematch"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val igdbClientId = localProperties.getProperty("IGDB_CLIENT_ID")
            ?: (project.findProperty("IGDB_CLIENT_ID") as? String)
            ?: ""
        val igdbClientSecret = localProperties.getProperty("IGDB_CLIENT_SECRET")
            ?: (project.findProperty("IGDB_CLIENT_SECRET") as? String)
            ?: ""

        buildConfigField("String", "IGDB_CLIENT_ID", "\"$igdbClientId\"")
        buildConfigField("String", "IGDB_CLIENT_SECRET", "\"$igdbClientSecret\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "GAMES_COLLECTION", "\"games_test\"")
        }
        release {
            buildConfigField("String", "GAMES_COLLECTION", "\"games\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Firebase BOM y módulos Auth + Firestore
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Retrofit & OkHttp para IGDB API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val exclusions = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/*Binding*.*",
        "**/*_Impl*.*",
        "**/ComposableSingletons*.*",
        "**/ui/theme/**",
        "**/*Screen*.class",
        "**/*Modal*.class",
        "**/*Components*.class",
        "**/*Card*.class",
        "**/*Item*.class",
        "**/*Dialog*.class",
        "**/MainActivity*.class"
    )

    val kotlinClasses = fileTree(
        layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    ) {
        exclude(exclusions)
    }
    val legacyKotlinClasses = fileTree(
        layout.buildDirectory.dir("tmp/kotlin-classes/debug")
    ) {
        exclude(exclusions)
    }
    val javaClasses = fileTree(
        layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    ) {
        exclude(exclusions)
    }

    classDirectories.setFrom(files(kotlinClasses, legacyKotlinClasses, javaClasses))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    )
}

sonar {
    isSkipProject = false
    properties {
        property("sonar.projectKey", "GameMatch")
        property("sonar.projectName", "GameMatch")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.java.binaries", "build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}