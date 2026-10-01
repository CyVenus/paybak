import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

/** A value from the untracked local.properties, blank when it isn't set. */
fun localProperty(key: String): String =
    Properties()
        .apply { rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use(::load) }
        .getProperty(key, "")

android {
    namespace = "app.paybak.paybak"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "app.paybak.paybak"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // The release key from `release.storeFile`, `release.storePassword`, `release.keyAlias` and
    // `release.keyPassword` in local.properties. Without them the release APK is left unsigned.
    val releaseStoreFile = localProperty("release.storeFile")
    if (releaseStoreFile.isNotBlank()) {
        signingConfigs.create("release") {
            storeFile = file(releaseStoreFile)
            storePassword = localProperty("release.storePassword")
            keyAlias = localProperty("release.keyAlias")
            keyPassword = localProperty("release.keyPassword")
        }
    }

    buildTypes {
        debug {
            // RevenueCat Test Store key: fake purchases, no Play account needed. The SDK refuses
            // to run with a Test Store key in a release build.
            buildConfigField("String", "REVENUECAT_API_KEY", "\"test_ZodzFFhityYkLvLvsuepQmsAeqo\"")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = false
            }
            // The RevenueCat Google Play public key (goog_…) from `revenuecat.playKey` in
            // local.properties. Left blank, Purchases isn't configured and Pro stays locked.
            buildConfigField("String", "REVENUECAT_API_KEY", "\"${localProperty("revenuecat.playKey")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // java.time on minSdk 24 (app-architecture §3.6).
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    // JVM unit tests read the debug-only demo seed (seed/demo.json) from the classpath.
    sourceSets.getByName("test").resources.directories.add("src/debug/assets")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.rive.android)
    implementation(libs.revenuecat.purchases)
    implementation(libs.revenuecat.purchases.ui)
    implementation(libs.zxing.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.play.services.code.scanner)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}