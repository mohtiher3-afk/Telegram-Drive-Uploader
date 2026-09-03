import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Base64
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildDir = file("C:/Users/acer/AppData/Local/Temp/tdg-build/app/build")

plugins {
  
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.telegramdrive.uploader"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.telegramdrive.prmuq"
    minSdk = 24
    targetSdk = 35
    versionCode = 20
    versionName = "1.0.20"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Package the verified TDLib v1.8.66 native artifacts for the supported ABIs.
    ndk {
      abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
    }
  }

  var releaseSigningConfigured = false
  signingConfigs {
    create("release") {
      val envKeystoreBase64 = System.getenv("RELEASE_KEYSTORE_BASE64")
      val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
        ?: project.findProperty("RELEASE_KEYSTORE_PATH")?.toString()
        ?: "${rootDir}/release.keystore"

      val keystoreFile = file(keystorePath)
      if (!envKeystoreBase64.isNullOrEmpty()) {
        try {
          val decoded = Base64.getDecoder().decode(envKeystoreBase64.trim())
          keystoreFile.parentFile.mkdirs()
          keystoreFile.writeBytes(decoded)
        } catch (_: Exception) {
          project.logger.error("Failed to decode RELEASE_KEYSTORE_BASE64 without exposing its value")
        }
      }

      val configuredStorePassword = System.getenv("RELEASE_STORE_PASSWORD")
        ?: project.findProperty("RELEASE_STORE_PASSWORD")?.toString()
      val configuredKeyAlias = System.getenv("RELEASE_KEY_ALIAS")
        ?: project.findProperty("RELEASE_KEY_ALIAS")?.toString()
      val configuredKeyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        ?: project.findProperty("RELEASE_KEY_PASSWORD")?.toString()
      if (keystoreFile.isFile && !configuredStorePassword.isNullOrBlank() &&
        !configuredKeyAlias.isNullOrBlank() && !configuredKeyPassword.isNullOrBlank()
      ) {
        storeFile = keystoreFile
        storePassword = configuredStorePassword
        keyAlias = configuredKeyAlias
        keyPassword = configuredKeyPassword
        releaseSigningConfigured = true
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  splits {
    abi {
      val requestedAbi = providers.gradleProperty("targetAbi").orNull
      isEnable = requestedAbi != null
      reset()
      val supportedAbis = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
      val selectedAbis = requestedAbi?.let { listOf(it) } ?: supportedAbis
      require(selectedAbis.all { it in supportedAbis }) {
        "targetAbi must be one of: ${supportedAbis.joinToString()}; got $requestedAbi"
      }
      include(*selectedAbis.toTypedArray())
      isUniversalApk = true
    }
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      if (releaseSigningConfigured) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(libs.androidx.hilt.work)
  ksp(libs.androidx.hilt.compiler)
  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.animation.graphics)
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.datastore.preferences)
  // implementation(libs.kotlogram)
  // implementation("org.telegram:tdlib:1.8.0") // Replace with latest version if possible, but 1.8.0 is a reasonable start if not specified
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
