import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.hilt)
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

// Kover is intentionally NOT applied to this module. TdApi.java (vendored TDLib
// bindings, ~137k lines and several hundred nested classes) overflows the JVM's
// bytecode probe arrays during instrumentation, which kills the test worker with
// "OutOfMemoryError: Illegal Capacity: -96150052" before any assertion runs.
// Report-level filters cannot prevent this because they apply after collection.
//
// Until TdApi moves to its own module, :data is excluded from coverage by design,
// not by accident. Do not report a coverage number for :data, and do not re-add the
// plugin here without that split. See docs/architecture/adr-001-tdlib-binaries.md.

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