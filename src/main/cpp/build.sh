#!/bin/bash
# RaveX Team — native library build script (https://github.com/StormDevzz/RaveX)
# Adapted for ThunderHack
#
# Builds the native optimizer for the current platform.
# Requires: cmake, C++17 compiler, JAVA_HOME pointing to a JDK 21+
#
# Usage: ./build.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="${SCRIPT_DIR}/build"

echo "[ThunderHack-Native] Configuring with CMake..."
cmake -S "${SCRIPT_DIR}" -B "${BUILD_DIR}" -DCMAKE_BUILD_TYPE=Release

echo "[ThunderHack-Native] Building..."
cmake --build "${BUILD_DIR}" --config Release --parallel

echo "[ThunderHack-Native] Done! Library placed in:"
echo "    ${SCRIPT_DIR}/../resources/assets/thunderhack/natives/"
ls -la "${SCRIPT_DIR}/../resources/assets/thunderhack/natives/" 2>/dev/null || true
