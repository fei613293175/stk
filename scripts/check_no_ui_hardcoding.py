#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--allow-missing-source", action="store_true")
args = p.parse_args()
source = ROOT / "android-app"
if not source.exists():
    if args.allow_missing_source:
        print("android-app 尚未建立：合同阶段允许缺失；进入编码后不得缺失")
        sys.exit(0)
    print("缺少 android-app 源码目录", file=sys.stderr)
    sys.exit(1)

patterns = {
    "direct_dp": re.compile(r"(?<![A-Za-z0-9_])\d+(?:\.\d+)?\.dp\b"),
    "direct_sp": re.compile(r"(?<![A-Za-z0-9_])\d+(?:\.\d+)?\.sp\b"),
    "direct_color": re.compile(r"\bColor\s*\("),
    "direct_shape": re.compile(r"\bRoundedCornerShape\s*\("),
}
violations = []
for f in source.rglob("*.kt"):
    rel = f.relative_to(ROOT).as_posix()
    if any(x in rel for x in ["core-designsystem", "/build/", "/src/test/", "/src/androidTest/"]):
        continue
    for lineno, line in enumerate(f.read_text(encoding="utf-8").splitlines(), 1):
        code = line.split("//", 1)[0]
        for name, rx in patterns.items():
            if rx.search(code):
                violations.append(f"{rel}:{lineno}:{name}:{line.strip()}")
if violations:
    print("业务 UI 发现硬编码，必须改用 STK Token：", file=sys.stderr)
    print("\n".join(violations[:200]), file=sys.stderr)
    sys.exit(1)
print("业务 UI 未发现被禁止的直接 dp/sp/Color/Shape 声明")
