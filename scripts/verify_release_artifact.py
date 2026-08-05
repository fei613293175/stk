#!/usr/bin/env python3
from __future__ import annotations

import argparse
import glob
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile
from datetime import datetime, timezone
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
VERSION_MAP = {"V1.0.0": ("1.0.0", 10000), "V1.1.0": ("1.1.0", 10100), "V1.2.0": ("1.2.0", 10200), "V1.3.0": ("1.3.0", 10300), "V1.4.0": ("1.4.0", 10400)}
p = argparse.ArgumentParser()
p.add_argument("--apk-glob", required=True)
p.add_argument("--normalize")
p.add_argument("--allow-small-test-artifact", action="store_true")
args = p.parse_args()
paths = [Path(x) for x in glob.glob(str(ROOT / args.apk_glob))]
if len(paths) != 1:
    print(f"必须精确找到一个 APK，实际 {len(paths)}: {paths}", file=sys.stderr)
    sys.exit(1)
apk = paths[0]
if not zipfile.is_zipfile(apk):
    print("APK 不是有效 ZIP/APK", file=sys.stderr)
    sys.exit(1)
size = apk.stat().st_size
if size < 10 * 1024 * 1024 and not args.allow_small_test_artifact:
    print(f"APK 实际大小小于 10MiB: {size}", file=sys.stderr)
    sys.exit(1)
if size < 10 * 1024 * 1024:
    print(f"测试构建体积低于正式交付门禁，仅用于在线编译验证: {size}", file=sys.stderr)
with zipfile.ZipFile(apk) as z:
    suspicious = [n for n in z.namelist() if any(x in n.lower() for x in ["dummy_padding", "filler_blob", "size_padding"])]
    if suspicious:
        print(f"发现疑似凑体积文件: {suspicious}", file=sys.stderr)
        sys.exit(1)

release_id = os.getenv("STK_RELEASE_ID") or (lambda d: d.get("current_release") or d.get("release_id"))(yaml.safe_load((ROOT / "CURRENT_RELEASE.yaml").read_text(encoding="utf-8")))
if release_id not in VERSION_MAP:
    print(f"未知发布版本: {release_id}", file=sys.stderr)
    sys.exit(1)
expected_name, expected_code = VERSION_MAP[release_id]
identity = {"application_id": None, "version_name": None, "version_code": None}

def run_optional(cmd):
    try:
        return subprocess.check_output(cmd, text=True, stderr=subprocess.STDOUT).strip()
    except (FileNotFoundError, subprocess.CalledProcessError):
        return None

apkanalyzer = shutil.which("apkanalyzer")
if apkanalyzer:
    identity["application_id"] = run_optional([apkanalyzer, "manifest", "application-id", str(apk)])
    identity["version_name"] = run_optional([apkanalyzer, "manifest", "version-name", str(apk)])
    identity["version_code"] = run_optional([apkanalyzer, "manifest", "version-code", str(apk)])
    if identity["application_id"] != "com.zzyihao.stk":
        print(f"applicationId 不正确: {identity['application_id']}", file=sys.stderr)
        sys.exit(1)
    if identity["version_name"] != expected_name or str(identity["version_code"]) != str(expected_code):
        print(f"版本不正确: {identity}, expected={expected_name}/{expected_code}", file=sys.stderr)
        sys.exit(1)
else:
    print("警告：未找到 apkanalyzer，CI Android 环境必须提供并执行身份检查", file=sys.stderr)

apksigner = shutil.which("apksigner")
if apksigner:
    result = subprocess.run([apksigner, "verify", "--verbose", "--print-certs", str(apk)], text=True, capture_output=True)
    if result.returncode != 0:
        print(result.stdout + result.stderr, file=sys.stderr)
        sys.exit(1)
    cert_output = result.stdout
else:
    cert_output = "APKSIGNER_NOT_AVAILABLE"
    print("警告：未找到 apksigner，CI Android 环境必须执行签名验证", file=sys.stderr)

sha = hashlib.sha256(apk.read_bytes()).hexdigest()
info = {
    "release_id": release_id, "version_name": expected_name, "version_code": expected_code,
    "application_id": "com.zzyihao.stk", "apk_source": str(apk.relative_to(ROOT)),
    "apk_size_bytes": size, "apk_sha256": sha, "manifest_identity": identity,
    "git_commit": os.getenv("GITHUB_SHA", run_optional(["git", "-C", str(ROOT), "rev-parse", "HEAD"]) or "UNKNOWN"),
    "github_run_id": os.getenv("GITHUB_RUN_ID", "LOCAL_VALIDATION"),
    "github_run_attempt": os.getenv("GITHUB_RUN_ATTEMPT", "LOCAL_VALIDATION"),
    "github_repository": os.getenv("GITHUB_REPOSITORY", "fei613293175/stk"),
    "verified_at_utc": datetime.now(timezone.utc).isoformat(),
    "signature_verification": cert_output[:4000],
    "artifact_rule": "This exact byte sequence must be tested and delivered; rebuilding is forbidden.",
}
print(json.dumps(info, ensure_ascii=False, indent=2))
if args.normalize:
    out = ROOT / args.normalize
    out.mkdir(parents=True, exist_ok=True)
    target = out / f"商推客-{expected_name}-release.apk"
    shutil.copy2(apk, target)
    info["normalized_apk"] = target.name
    (out / "BUILD_INFO.json").write_text(json.dumps(info, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (out / "SHA256SUMS.txt").write_text(f"{sha}  {target.name}\n", encoding="utf-8")
