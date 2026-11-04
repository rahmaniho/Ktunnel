plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ir.karensoft.karentunnel.vpn"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Required by com.wireguard.android:tunnel (see its README, "Embedding").
        isCoreLibraryDesugaringEnabled = true
    }


    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    api(project(":core"))

    // The official WireGuard tunnel library. No other crypto dependency is
    // permitted here — see docs/SECURITY-REPORT.md.
    implementation(libs.wireguard.tunnel)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
}

// Top-level Kotlin extension (NOT nested in android{}): the AGP 8.13 +
// Kotlin 2.x contract. Warnings are errors on purpose.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
        allWarningsAsErrors.set(true)
    }
}
