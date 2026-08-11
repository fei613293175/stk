#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_FILE="$ROOT/android/app/build.gradle.kts"
VERSION="$(sed -n 's/^[[:space:]]*versionName = "\([^"]*\)"/\1/p' "$BUILD_FILE" | head -n 1)"
if [[ -n "${STK_APK_SOURCE:-}" ]]; then
    APK_SOURCE="$STK_APK_SOURCE"
elif [[ -f "$ROOT/android/app/build/outputs/apk/release/app-release.apk" ]]; then
    APK_SOURCE="$ROOT/android/app/build/outputs/apk/release/app-release.apk"
else
    APK_SOURCE="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
fi
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+-(alpha|beta|rc)\.[0-9]+$ ]]; then
    echo "ERROR: 无法从 Gradle 配置读取 Beta 版本：$VERSION" >&2
    exit 1
fi
if [[ ! -f "$APK_SOURCE" ]]; then
    echo "ERROR: APK 不存在：$APK_SOURCE" >&2
    exit 1
fi
DELIVERY="$ROOT/deliveries/$VERSION"
mkdir -p "$DELIVERY"
cp "$APK_SOURCE" "$DELIVERY/STK-$VERSION.apk"
sha256sum "$DELIVERY/STK-$VERSION.apk" | awk '{print $1}' > "$DELIVERY/APK_SHA256.txt"
git -C "$ROOT" rev-parse HEAD > "$DELIVERY/COMMIT.txt" 2>/dev/null || echo "UNCOMMITTED_SCAFFOLD" > "$DELIVERY/COMMIT.txt"
cp "$ROOT/templates/FUNCTION_COMPLETION.md" "$DELIVERY/FUNCTION_COMPLETION.md"
cp "$ROOT/templates/KNOWN_ISSUES.md" "$DELIVERY/KNOWN_ISSUES.md"
cp "$ROOT/templates/OWNER_TEST_GUIDE.md" "$DELIVERY/OWNER_TEST_GUIDE.md"
echo "DELIVERED: $DELIVERY"
