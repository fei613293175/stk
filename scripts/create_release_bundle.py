#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
ORDER = ["V1.0.0", "V1.1.0", "V1.2.0", "V1.3.0", "V1.4.0"]
p = argparse.ArgumentParser()
p.add_argument("--source", required=True)
p.add_argument("--output", required=True)
args = p.parse_args()
source = ROOT / args.source
out = ROOT / args.output
release_id = os.getenv("STK_RELEASE_ID") or (lambda d: d.get("current_release") or d.get("release_id"))(yaml.safe_load((ROOT / "CURRENT_RELEASE.yaml").read_text(encoding="utf-8")))
if release_id not in ORDER:
    print("未知 release_id", file=sys.stderr); sys.exit(1)
evidence_path = ROOT / "release-inputs" / release_id / "RELEASE_EVIDENCE.yaml"
evidence = yaml.safe_load(evidence_path.read_text(encoding="utf-8"))
required_status = ["READY_FOR_DELIVERY", "PASS", "PASS", "PASS", "PASS"]
actual_status = [evidence.get("status"), evidence.get("automated_test_status"), evidence.get("visual_status"), evidence.get("upgrade_status"), evidence.get("deployment_status")]
if actual_status != required_status:
    print(f"发布证据未关闭: expected={required_status}, actual={actual_status}", file=sys.stderr); sys.exit(1)
features = yaml.safe_load((ROOT / "contracts/feature-map.yaml").read_text(encoding="utf-8"))["features"]
version_features = [f for f in features if f["release_id"] == release_id]
feature_statuses = evidence.get("feature_statuses", {})
missing_done = [f["id"] for f in version_features if feature_statuses.get(f["id"]) != "DONE"]
if missing_done:
    print(f"Feature 未完成: {missing_done}", file=sys.stderr); sys.exit(1)

release_dir = source / "release"
apks = list(release_dir.rglob("*.apk"))
if len(apks) != 1:
    print(f"accepted source 必须精确包含一个 APK: {apks}", file=sys.stderr); sys.exit(1)
build_info_files = list(release_dir.rglob("BUILD_INFO.json"))
if len(build_info_files) != 1:
    print("缺少唯一 BUILD_INFO.json", file=sys.stderr); sys.exit(1)
build_info = json.loads(build_info_files[0].read_text(encoding="utf-8"))
if build_info["release_id"] != release_id:
    print("BUILD_INFO 与发布版本不一致", file=sys.stderr); sys.exit(1)
sha = hashlib.sha256(apks[0].read_bytes()).hexdigest()
if sha != build_info["apk_sha256"]:
    print("APK SHA 与 BUILD_INFO 不一致", file=sys.stderr); sys.exit(1)

if out.exists(): shutil.rmtree(out)
out.mkdir(parents=True)
apk_name = f"商推客-{build_info['version_name']}-release.apk"
shutil.copy2(apks[0], out / apk_name)
shutil.copy2(build_info_files[0], out / "BUILD_INFO.json")

planned = [f"# 商推客 {build_info['version_name']} 功能计划", ""]
completed = [f"# 商推客 {build_info['version_name']} 功能完成清单", ""]
for f in version_features:
    planned += [f"## {f['id']} {f['name']}", f"- 范围：{f.get('user_value','')}", f"- 页面：{', '.join(f.get('page_ids', [])) or '无用户页面'}", ""]
    completed += [f"- [x] **{f['id']} {f['name']}**：DONE；证据按 Test ID 和 CI Artifact 索引。"]
(out / "FEATURES_PLANNED.md").write_text("\n".join(planned) + "\n", encoding="utf-8")
(out / "FEATURES_COMPLETED.md").write_text("\n".join(completed) + "\n", encoding="utf-8")

owner_source = ROOT / "versions" / release_id / "TEST_AND_OWNER_ACCEPTANCE.md"
owner_text = owner_source.read_text(encoding="utf-8") + "\n\n> 当前状态：PENDING_OWNER。此文件供项目所有者安装桌面 APK 后逐项填写，CI 不得代替人工验收。\n"
(out / "OWNER_TEST_CHECKLIST.md").write_text(owner_text, encoding="utf-8")

(out / "AUTOMATED_TEST_REPORT.md").write_text(
    f"# 自动测试报告\n\n- Release：{release_id}\n- Commit：{build_info['git_commit']}\n- GitHub Run：{build_info['github_run_id']}\n- 合同：PASS\n- Android/后端：PASS\n- API 36 模拟器：PASS\n- 视觉：PASS\n- 覆盖安装：PASS\n\n原始报告位于同一 Accepted Artifact 的 evidence 目录，禁止用本文件代替原始 JUnit、截图和日志。\n",
    encoding="utf-8")

with (ROOT / "contracts/mockup-manifest.csv").open(newline="", encoding="utf-8-sig") as f:
    manifest = list(csv.DictReader(f))
release_index = ORDER.index(release_id)
applicable = [r for r in manifest if ORDER.index(r["release_id"]) <= release_index and r["status"] == "APPROVED"]
with (out / "UI_SCREENSHOT_INDEX.csv").open("w", newline="", encoding="utf-8-sig") as f:
    fields = ["state_id", "mockup_id", "baseline_path", "baseline_sha256", "actual_path", "visual_status"]
    w = csv.DictWriter(f, fieldnames=fields); w.writeheader()
    for r in applicable:
        w.writerow({"state_id": r["state_id"], "mockup_id": r["mockup_id"], "baseline_path": r["file_path"], "baseline_sha256": r["sha256"], "actual_path": f"evidence/emulator/screenshots/{r['state_id']}.png", "visual_status": "PASS"})

visual_candidates = list((source / "evidence").rglob("VISUAL_DIFF_REPORT.md"))
if not visual_candidates:
    print("缺少视觉差异报告", file=sys.stderr); sys.exit(1)
shutil.copy2(visual_candidates[0], out / "VISUAL_DIFF_REPORT.md")

domains = yaml.safe_load((ROOT / "contracts/domain-delivery-map.yaml").read_text(encoding="utf-8"))["domains"]
endpoint_lines = [f"# {release_id} 部署端点", "", "| 域名 | 用途 | 本版状态 |", "|---|---|---|"]
for d in domains:
    if d["service_required_in"] in ORDER[:release_index+1] or d["dns_required_in"] == "EXISTING":
        endpoint_lines.append(f"| {d['host']} | {d['purpose']} | {evidence.get('deployment_endpoints',{}).get(d['host'],'PASS/见部署日志')} |")
(out / "DEPLOYMENT_ENDPOINTS.md").write_text("\n".join(endpoint_lines) + "\n", encoding="utf-8")
shutil.copy2(ROOT / "versions" / release_id / "DNS_ACTION_REQUIRED.md", out / "DNS_ACTION_REQUIRED.md")

issues = evidence.get("known_issues", [])
issue_lines = [f"# {release_id} 已知问题", ""] + (["- 无已知问题。"] if not issues else [f"- {x}" for x in issues])
(out / "KNOWN_ISSUES.md").write_text("\n".join(issue_lines) + "\n", encoding="utf-8")
provenance = {
    "release_id": release_id, "version_name": build_info["version_name"], "version_code": build_info["version_code"],
    "git_commit": build_info["git_commit"], "github_run_id": build_info["github_run_id"],
    "apk_sha256": sha, "source_artifact": "stk-exact-release-<commit>",
    "accepted_artifact": "stk-accepted-delivery-<commit>", "created_at_utc": datetime.now(timezone.utc).isoformat(),
    "no_rebuild_after_acceptance": True,
}
(out / "CI_PROVENANCE.json").write_text(json.dumps(provenance, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

# Preserve raw evidence in a subdirectory while keeping the owner-facing root concise.
if (source / "evidence").exists():
    shutil.copytree(source / "evidence", out / "evidence")

hash_lines = []
for f in sorted(p for p in out.rglob("*") if p.is_file() and p.name != "SHA256SUMS.txt"):
    hash_lines.append(f"{hashlib.sha256(f.read_bytes()).hexdigest()}  {f.relative_to(out).as_posix()}")
(out / "SHA256SUMS.txt").write_text("\n".join(hash_lines) + "\n", encoding="utf-8")
print(f"accepted bundle: {out}")
