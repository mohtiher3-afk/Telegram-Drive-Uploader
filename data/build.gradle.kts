import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kover)
}

android {
  namespace = "com.telegramdrive.uploader.data"
  compileSdk = 37

  defaultConfig {
    minSdk = 24
    buildConfigField("String", "VERSION_NAME", "\"${rootProject.extra["appVersionName"]}\"")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    buildConfig = true
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }
  lint {
    warningsAsErrors = true
    abortOnError = true
    checkDependencies = false
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
    freeCompilerArgs.add("-Xannotation-default-target=param-property")
  }
}

// JVM test coverage for this module.
//   ./gradlew :data:koverHtmlReport   -> data/build/reports/kover/html/index.html
//   ./gradlew :data:koverXmlReport    -> data/build/reports/kover/report.xml
// onCheck makes koverVerify emit both formats alongside its rule check.
kover {
  reports {
    filters {
      excludes {
        // Vendored generated TDLib bindings. TdApi.java alone is ~137k lines, and
        // instrumenting its several hundred nested classes overflows Kover's
        // coverage data structure ("Illegal Capacity: -96150052").
        classes("org.drinkless.tdlib.*")

        // DI and build-generated code only. Note that *_Impl is deliberately NOT
        // excluded: in this project the *Impl classes are hand-written production
        // code (TelegramClientImpl, TelegramUploadEngineImpl, ...), and measuring
        // them is the point of collecting coverage.
        classes(
          "*_Factory",
          "*_HiltModules*",
          "*.BuildConfig",
          "*.Hilt_*",
          "*.Dagger*",
          "*_Provide*Factory",
          "*_MembersInjector"
        )
      }
    }
    total {
      html { onCheck.set(true) }
      xml { onCheck.set(true) }
    }
  }
}

// TDLib API credentials are injected from .env / .env.example as BuildConfig fields.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":core"))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.hilt.work)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.hilt.android)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  ksp(libs.androidx.hilt.compiler)
  ksp(libs.hilt.android.compiler)
  "ksp"(libs.androidx.room.compiler)

  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.work.testing)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
}