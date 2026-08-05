plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose") }
val releaseKeystorePath = System.getenv("STK_RELEASE_KEYSTORE_PATH")
val releaseStorePassword = System.getenv("STK_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = System.getenv("STK_RELEASE_KEY_ALIAS")
val releaseKeyPassword = System.getenv("STK_RELEASE_KEY_PASSWORD")
android { namespace = "com.zzyihao.stk"; compileSdk = 37
    defaultConfig { applicationId = "com.zzyihao.stk"; minSdk = 26; targetSdk = 37; versionCode = 10000; versionName = "1.0.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; testBuildType = "release" }
    buildFeatures { compose = true; buildConfig = true }
    signingConfigs {
        create("release") {
            require(!releaseKeystorePath.isNullOrBlank()) { "STK_RELEASE_KEYSTORE_PATH is required for release builds" }
            storeFile = file(releaseKeystorePath!!)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }
    buildTypes { getByName("release") { signingConfig = signingConfigs.getByName("release") } }
}
dependencies {
    implementation(project(":core-designsystem"))
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    debugImplementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Release instrumentation runs the same release target APK, so its Compose host must be present there.
    releaseImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
