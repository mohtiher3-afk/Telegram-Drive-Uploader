import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Mirror :app's Windows workaround: keep Gradle output outside the OneDrive-synced
// project directory. On Linux/macOS CI the default project-relative build dir is used.
if (System.getProperty("os.name").lowercase().contains("win")) {
  buildDir = file("${System.getProperty("java.io.tmpdir")}/tdg-build/benchmark/build")
}

plugins {
  alias(libs.plugins.android.test)
  alias(libs.plugins.androidx.baselineprofile)
}

android {
  namespace = "com.telegramdrive.uploader.benchmark"
  compileSdk = 37

  defaultConfig {
    minSdk = 30
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    targetProjectPath = ":app"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildTypes {
    // The Baseline Profiles plugin drives this variant. It needs a debuggable,
    // profileable, debug-signed build of :app to record against.
    create("benchmark") {
      isDebuggable = true
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("debug")
    }
  }

  // Required by AGP for com.android.test modules.
  experimentalProperties["android.experimental.self-instrumenting"] = true

  lint {
    warningsAsErrors = true
    abortOnError = true
    checkDependencies = false
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

baselineProfile {
  // Baseline profile generation and macrobenchmarks are instrumented: they need a
  // connected device or a running emulator. There is no JVM-side fallback.
  useConnectedDevices = true
}

dependencies {
  implementation(libs.androidx.junit)
  implementation(libs.androidx.benchmark.macro.junit4)
}
