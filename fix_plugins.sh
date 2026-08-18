#!/bin/bash
# Remove hilt from where it is
sed -i '/alias(libs.plugins.hilt)/d' app/build.gradle.kts
sed -i '/alias(libs.plugins.hilt) apply false/d' build.gradle.kts

# Insert it at the end of plugins block
sed -i 's/alias(libs.plugins.google.services)/alias(libs.plugins.google.services)\n  alias(libs.plugins.hilt)/g' app/build.gradle.kts
sed -i 's/alias(libs.plugins.google.services) apply false/alias(libs.plugins.google.services) apply false\n  alias(libs.plugins.hilt) apply false/g' build.gradle.kts

