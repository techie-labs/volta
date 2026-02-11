#!/bin/bash

# Executes the full development workflow.
# Sequence: Clean -> Format -> Lint -> Test -> Build

set -e # Abort script on any command failure

echo "🚀 Starting Development Workflow..."

echo "🧹 [1/5] Cleaning project..."
./gradlew clean

echo "🎨 [2/5] Applying code formatting (Spotless)..."
./gradlew spotlessApply

echo "🔍 [3/5] Running static analysis (Detekt)..."
./gradlew detekt

echo "🧪 [4/5] Running tests..."
./gradlew allTests

echo "🏗️  [5/5] Building project..."
./gradlew build

echo "✅ Workflow completed successfully! Your code is ready."
