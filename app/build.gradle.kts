import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * 릴리스 서명 정보는 `keystore.properties` 에서 읽는다 (`.gitignore` 됨, 양식은 `keystore.properties.example`).
 * 파일이 없으면 release 는 서명 없이 만들어진다 — 스토어에 올릴 수는 없지만 빌드 자체는 깨지지 않게 한다.
 *
 * `providers.fileContents` 를 쓰는 이유는 `:data:festival` 의 `local.properties` 와 같다.
 * configuration cache 가 이 파일을 입력으로 추적해야 값이 바뀔 때 캐시가 깨진다.
 */
val keystoreProperties = Properties().apply {
    val text = providers
        .fileContents(rootProject.layout.projectDirectory.file("keystore.properties"))
        .asText
        .getOrElse("")
    load(text.reader())
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile").orEmpty().isNotBlank()

android {
    namespace = "com.mosstis.kofest"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.mosstis.kofest"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        // 스토어에 올릴 때마다 versionCode 를 1 올린다. 같은 값은 Play Console 이 거부한다.
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // 스토어 빌드와 개발 빌드를 한 기기에 같이 둘 수 있게 패키지명을 나눈다.
            // 데이터(DataStore·이미지 캐시)도 패키지별로 따로 쌓인다.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // R8 — 코드 축소·난독화·리소스 축소. keep rule 은 src/main/keepRules/ 에 둔다.
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // minSdk 24 에서 java.time 을 쓰기 위해 필요하다.
        isCoreLibraryDesugaringEnabled = true
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":feature:festival"))
    implementation(project(":data:festival"))

    implementation(libs.androidx.core.ktx)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
