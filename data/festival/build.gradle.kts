import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * API 키와 Base URL 은 `local.properties` 에서 읽는다.
 * 이 파일은 `.gitignore` 되어 있어 키가 저장소에 들어가지 않는다.
 *
 * `providers.fileContents` 를 쓰는 이유는 configuration cache 가 이 파일을 입력으로
 * 추적하게 하기 위해서다. `File.readText()` 로 읽으면 값이 바뀌어도 캐시가 안 깨진다.
 */
val localProperties = Properties().apply {
    val text = providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .getOrElse("")
    load(text.reader())
}

android {
    namespace = "com.mosstis.kofest.data.festival"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        buildConfigField(
            "String",
            "KOFEST_BASE_URL",
            "\"${localProperties.getProperty("kofest.baseUrl", "https://kofest.mosstis.com/")}\"",
        )
        buildConfigField(
            "String",
            "KOFEST_API_KEY",
            "\"${localProperties.getProperty("kofest.apiKey", "")}\"",
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // minSdk 24 에서 java.time 을 쓰기 위해 필요하다.
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":domain:festival"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
