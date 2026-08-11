import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

val apiEndpoint = providers.gradleProperty("stkApiEndpoint")
    .orElse("https://stk.zz-yihao.com/plugin.php?id=stk_auth:api")
val projectApiEndpoint = providers.gradleProperty("stkProjectApiEndpoint")
    .orElse("https://stk.zz-yihao.com/plugin.php?id=stk_project:api")
val useFakeBackend = providers.gradleProperty("stkUseFakeBackend")
    .orElse("false")
val jvmBytecodeTarget = providers.gradleProperty("stkJvmTarget")
    .orElse("17")
    .get()

android {
    namespace = "com.zzyihao.stk"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.zzyihao.stk"
        minSdk = 26
        targetSdk = 36
        versionCode = 10208
        versionName = "1.2.0-beta.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "STK_API_ENDPOINT", "\"${apiEndpoint.get()}\"")
        buildConfigField("String", "STK_PROJECT_API_ENDPOINT", "\"${projectApiEndpoint.get()}\"")
        buildConfigField("boolean", "STK_USE_FAKE_BACKEND", useFakeBackend.get())
        buildConfigField("String", "STK_UPDATE_MANIFEST_URL", "\"https://stk.zz-yihao.com/plugin.php?id=stk_project:api&resource=release/current\"")
    }

    signingConfigs {
        create("beta") {
            storeFile = file("../keystore/stk-beta.jks")
            storePassword = "stkbeta2026"
            keyAlias = "stk-beta"
            keyPassword = "stkbeta2026"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("beta")
            applicationIdSuffix = ""
            versionNameSuffix = ""
        }
        release {
            signingConfig = signingConfigs.getByName("beta")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmBytecodeTarget)
        targetCompatibility = JavaVersion.toVersion(jvmBytecodeTarget)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    // CI and release policy target JDK 17; local runtime supplies JDK 21,
    // which is compatible with the locked AGP/Kotlin toolchain.
    jvmToolchain(providers.gradleProperty("stkJvmToolchain").orElse("17").get().toInt())
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(jvmBytecodeTarget))
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
