#!/bin/bash

# Wrapper script for checking dependency updates
# This script automatically passes the required flags to work around
# configuration cache and parallel execution incompatibilities

echo "═══════════════════════════════════════════════════════════"
echo "  Checking for Dependency Updates"
echo "═══════════════════════════════════════════════════════════"
echo ""

# Run with required flags
./gradlew checkUpdates --no-parallel --no-configuration-cache "$@"

exit_code=$?

if [ $exit_code -eq 0 ]; then
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo "✅ Dependency check completed successfully"
    echo "═══════════════════════════════════════════════════════════"
else
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo "❌ Dependency check failed with exit code: $exit_code"
    echo "═══════════════════════════════════════════════════════════"
fi

exit $exit_code
