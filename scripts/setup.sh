#!/bin/bash

# Setup script for Volta project

echo "⚡ Setting up Volta environment..."

# 1. Check for local.properties
if [ ! -f "local.properties" ]; then
    echo "Creating local.properties from template..."
    cp local.properties.template local.properties
    echo "⚠️  Please update local.properties with your Android SDK path."
else
    echo "✅ local.properties exists."
fi

# 2. Make gradlew executable
echo "Making gradlew executable..."
chmod +x gradlew

# 3. Setup Git Hooks
if [ -d ".git" ]; then
    echo "Setting up git hooks..."
    cp scripts/pre-commit .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo "✅ Pre-commit hook installed."
else
    echo "⚠️  .git directory not found. Skipping git hooks setup."
fi

echo "✅ Setup complete! You can now run './gradlew build' to verify the project."
