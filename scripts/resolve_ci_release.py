#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import re
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
MAP = {
    "V1.0.0": ("1.0.0", "10000", ""),
    "V1.1.0": ("1.1.0", "10100", "stk-android-v1.0.0"),
    "V1.2.0": ("1.2.0", "10200", "stk-android-v1.1.0"),
    "V1.3.0": ("1.3.0", "10300", "stk-android-v1.2.0"),
    "V1.4.0": ("1.4.0", "10400", "stk-android-v1.3.0"),
}
p = argparse.ArgumentParser()
p.add_argument("--write-github-env", action="store_true")
args = p.parse_args()
release = os.getenv("STK_RELEASE_ID", "").strip()
ref = os.getenv("GITHUB_REF_NAME", "")
if not release:
    m = re.search(r"(?:stk-android-v|release/)(\d+\.\d+\.\d+)", ref)
    if m:
        release = "V" + m.group(1)
if not release:
    release = (lambda d: d.get("current_release") or d.get("release_id"))(yaml.safe_load((ROOT / "CURRENT_RELEASE.yaml").read_text(encoding="utf-8")))
if release not in MAP:
    print(f"无法解析发布版本: {release}", file=sys.stderr)
    sys.exit(1)
name, code, previous = MAP[release]
values = {"STK_RELEASE_ID": release, "STK_VERSION_NAME": name, "STK_VERSION_CODE": code, "STK_PREVIOUS_TAG_DEFAULT": previous}
print("\n".join(f"{k}={v}" for k, v in values.items()))
if args.write_github_env:
    env_file = os.getenv("GITHUB_ENV")
    if not env_file:
        print("缺少 GITHUB_ENV", file=sys.stderr)
        sys.exit(1)
    with open(env_file, "a", encoding="utf-8") as f:
        for k, v in values.items():
            f.write(f"{k}={v}\n")
