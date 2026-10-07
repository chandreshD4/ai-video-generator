plugins {
    id("com.android.application")
}

android {
    namespace = "com.aivideogenerator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aivideogenerator"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
}
