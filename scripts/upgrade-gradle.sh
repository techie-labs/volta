#!/bin/bash

# Upgrades the Gradle Wrapper and verifies the build.
# Usage: ./scripts/upgrade-gradle.sh <version>

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "❌ Error: Please provide a target Gradle version."
    echo "Usage: ./scripts/upgrade-gradle.sh <version>"
    echo "Example: ./scripts/upgrade-gradle.sh 8.7"
    exit 1
fi

echo "🚀 Upgrading Gradle Wrapper to version $VERSION..."

# 1. Update Wrapper
./gradlew wrapper --gradle-version "$VERSION" --distribution-type all
if [ $? -ne 0 ]; then
    echo "❌ Failed to upgrade Gradle Wrapper."
    exit 1
fi

echo "✅ Gradle Wrapper updated."
echo "🧹 Cleaning project to remove old artifacts..."

# 2. Clean Project
./gradlew clean
if [ $? -ne 0 ]; then
    echo "❌ Clean failed."
    exit 1
fi

echo "🏗️  Verifying build with the new Gradle version..."

# 3. Build Project (Verification)
./gradlew build -x test
if [ $? -ne 0 ]; then
    echo "❌ Build failed with Gradle $VERSION. Please check the logs."
    exit 1
fi

echo "🎉 Success! Gradle upgraded to $VERSION and build has been verified."
