#!/bin/bash
NEW_PKG="com.telegramdrive.uploader"
OLD_PKG="com.example"

# Change namespace in build.gradle.kts
sed -i "s/namespace = \"com.example\"/namespace = \"$NEW_PKG\"/g" app/build.gradle.kts
# Change applicationId
sed -i "s/applicationId = \"com.aistudio.tgdrive.upld\"/applicationId = \"$NEW_PKG\"/g" app/build.gradle.kts

# Move directories
mkdir -p app/src/main/java/com/telegramdrive/uploader
mkdir -p app/src/test/java/com/telegramdrive/uploader
mkdir -p app/src/androidTest/java/com/telegramdrive/uploader

cp -r app/src/main/java/com/example/* app/src/main/java/com/telegramdrive/uploader/
rm -rf app/src/main/java/com/example

cp -r app/src/test/java/com/example/* app/src/test/java/com/telegramdrive/uploader/ 2>/dev/null
rm -rf app/src/test/java/com/example 2>/dev/null

# Replace package imports
find app/src -type f -name "*.kt" -exec sed -i "s/package com.example/package $NEW_PKG/g" {} +
find app/src -type f -name "*.kt" -exec sed -i "s/import com.example/import $NEW_PKG/g" {} +

# AndroidManifest
sed -i "s/android:name=\".TelegramDriveApp\"/android:name=\"$NEW_PKG.TelegramDriveApp\"/g" app/src/main/AndroidManifest.xml
sed -i "s/android:name=\".MainActivity\"/android:name=\"$NEW_PKG.MainActivity\"/g" app/src/main/AndroidManifest.xml
