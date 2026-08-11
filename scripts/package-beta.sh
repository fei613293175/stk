#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK_SOURCE="$ROOT/android/app/build/outputs/apk/release/app-release.apk"
VERSION="1.0.0-beta.2"
DELIVERY="$ROOT/deliveries/$VERSION"
if [[ ! -f "$APK_SOURCE" ]]; then
    echo "ERROR: APK 不存在：$APK_SOURCE" >&2
    exit 1
fi
mkdir -p "$DELIVERY"
cp "$APK_SOURCE" "$DELIVERY/STK-$VERSION.apk"
sha256sum "$DELIVERY/STK-$VERSION.apk" | awk '{print $1}' > "$DELIVERY/APK_SHA256.txt"
git -C "$ROOT" rev-parse HEAD > "$DELIVERY/COMMIT.txt" 2>/dev/null || echo "UNCOMMITTED_SCAFFOLD" > "$DELIVERY/COMMIT.txt"
cp "$ROOT/templates/FUNCTION_COMPLETION.md" "$DELIVERY/FUNCTION_COMPLETION.md"
cp "$ROOT/templates/KNOWN_ISSUES.md" "$DELIVERY/KNOWN_ISSUES.md"
cp "$ROOT/templates/OWNER_TEST_GUIDE.md" "$DELIVERY/OWNER_TEST_GUIDE.md"
echo "DELIVERED: $DELIVERY"
