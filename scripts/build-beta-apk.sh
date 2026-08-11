#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
"$ROOT/scripts/fast-check.sh"
GRADLE_ARGS=(--no-daemon testReleaseUnitTest lintRelease assembleRelease)
if [[ -n "${STK_JVM_TOOLCHAIN:-}" ]]; then
    GRADLE_ARGS=("-PstkJvmToolchain=${STK_JVM_TOOLCHAIN}" "-PstkJvmTarget=${STK_JVM_TOOLCHAIN}" "${GRADLE_ARGS[@]}")
fi
"$ROOT/gradlew" "${GRADLE_ARGS[@]}"
"$ROOT/scripts/package-beta.sh"
