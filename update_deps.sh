#!/bin/bash

# Update libs.versions.toml
sed -i '/\[versions\]/a \
hilt = "2.51.1"\
hiltNavigationCompose = "1.2.0"\
workManager = "2.10.0"
' gradle/libs.versions.toml

sed -i '/\[libraries\]/a \
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }\
hilt-android-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }\
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }\
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
' gradle/libs.versions.toml

sed -i '/\[plugins\]/a \
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
' gradle/libs.versions.toml

# Update root build.gradle.kts
sed -i 's/plugins {/plugins {\n  alias(libs.plugins.hilt) apply false/g' build.gradle.kts

# Update app/build.gradle.kts
sed -i 's/plugins {/plugins {\n  alias(libs.plugins.hilt)\n  id("kotlin-kapt")/g' app/build.gradle.kts

sed -i '/dependencies {/a \
  implementation(libs.hilt.android)\n  kapt(libs.hilt.android.compiler)\n  implementation(libs.androidx.hilt.navigation.compose)\n  implementation(libs.androidx.work.runtime.ktx)
' app/build.gradle.kts

