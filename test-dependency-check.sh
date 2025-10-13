#!/bin/bash

# Test script for dependency check fix
# This script verifies that the dependency check works with configuration cache disabled

set -e

echo "═══════════════════════════════════════════════════════════"
echo "  Testing Dependency Check Fix"
echo "═══════════════════════════════════════════════════════════"

echo ""
echo "Step 1: Clean Gradle configuration cache..."
rm -rf ~/.gradle/configuration-cache
rm -rf .gradle/configuration-cache
rm -rf build/reports/configuration-cache
echo "✅ Configuration cache cleaned"

echo ""
echo "Step 2: Clean dependency check database..."
./gradlew cleanDependencyCheckDb --no-daemon --no-configuration-cache

echo ""
echo "Step 3: Run dependency check with all caches disabled..."
./gradlew dependencyCheckAnalyze \
  --no-daemon \
  --no-configuration-cache \
  --no-build-cache \
  --rerun-tasks \
  --stacktrace

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  ✅ Dependency check completed successfully!"
echo "═══════════════════════════════════════════════════════════"
