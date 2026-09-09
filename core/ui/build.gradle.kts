plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mosstis.kofest.core.ui"

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

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:designsystem"))

    implementation(platform(libs.androidx.compose.bom))

    // BaseActivity 가 ComponentActivity 를 상속하고 @Composable 을 공개하므로 api 로 둔다.
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}
