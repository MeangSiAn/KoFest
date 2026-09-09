plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mosstis.kofest.core.presentation"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:common"))

    // BaseViewModel 의 상위 타입과 공개 시그니처(StateFlow/Flow/Job)에 노출되므로 api 로 둔다.
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)

    implementation(libs.kotlinx.coroutines.android)
}
