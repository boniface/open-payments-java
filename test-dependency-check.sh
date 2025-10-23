#!/bin/bash

# Test script for dependency check fix
# This script verifies that the dependency check works with configuration cache disabled

set -e

echo "═══════════════════════════════════════════════════════════"
echo "  Testing Dependency Check Fix"
echo "═══════════════════════════════════════════════════════════"

echo ""
echo "Step 1: Clean dependency check database..."
./gradlew cleanDependencyCheckDb --no-daemon --no-configuration-cache

echo ""
echo "Step 2: Run dependency check with configuration cache disabled..."
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

