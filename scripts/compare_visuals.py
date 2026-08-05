#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import os
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageEnhance

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--release", required=True)
p.add_argument("--actual-root", default="artifacts/emulator/screenshots")
p.add_argument("--output-root", default="artifacts/visual")
p.add_argument("--fail-on-diff", action="store_true")
p.add_argument("--threshold", type=float, default=float(os.getenv("STK_VISUAL_DIFF_RATIO", "0.001")))
p.add_argument("--channel-tolerance", type=int, default=int(os.getenv("STK_VISUAL_CHANNEL_TOLERANCE", "6")))
args = p.parse_args()

with (ROOT / "contracts/mockup-manifest.csv").open(newline="", encoding="utf-8-sig") as f:
    rows = [r for r in csv.DictReader(f) if r["release_id"] == args.release and r["status"] == "APPROVED"]
if not rows:
    print("本版没有 APPROVED 效果图，不能执行视觉验收", file=sys.stderr)
    sys.exit(2)
actual_root = ROOT / args.actual_root
out_root = ROOT / args.output_root
(out_root / "diff").mkdir(parents=True, exist_ok=True)
results = []
failures = []
for r in rows:
    baseline_path = ROOT / r["file_path"]
    actual_path = actual_root / f"{r['state_id']}.png"
    result = {"mockup_id": r["mockup_id"], "state_id": r["state_id"], "baseline": str(baseline_path.relative_to(ROOT)), "actual": str(actual_path.relative_to(ROOT)) if actual_path.exists() else str(actual_path), "threshold": args.threshold}
    if not baseline_path.is_file() or not actual_path.is_file():
        result.update({"status": "MISSING", "diff_ratio": 1.0})
        failures.append(result)
        results.append(result)
        continue
    baseline = Image.open(baseline_path).convert("RGBA")
    actual = Image.open(actual_path).convert("RGBA")
    expected_size = tuple(int(x) for x in r["pixel_size"].lower().split("x"))
    if baseline.size != expected_size or actual.size != expected_size:
        result.update({"status": "SIZE_MISMATCH", "baseline_size": baseline.size, "actual_size": actual.size, "diff_ratio": 1.0})
        failures.append(result)
        results.append(result)
        continue
    diff = ImageChops.difference(baseline, actual).convert("RGB")
    pixels = list(diff.getdata())
    changed = sum(1 for px in pixels if max(px) > args.channel_tolerance)
    ratio = changed / max(1, len(pixels))
    diff_path = out_root / "diff" / f"{r['state_id']}.png"
    ImageEnhance.Contrast(diff).enhance(4.0).save(diff_path)
    status = "PASS" if ratio <= args.threshold else "FAIL"
    result.update({"status": status, "diff_ratio": ratio, "changed_pixels": changed, "total_pixels": len(pixels), "diff": str(diff_path.relative_to(ROOT))})
    if status != "PASS":
        failures.append(result)
    results.append(result)

report = {"release_id": args.release, "threshold": args.threshold, "channel_tolerance": args.channel_tolerance, "total": len(results), "passed": sum(r["status"] == "PASS" for r in results), "failed": len(failures), "results": results}
(out_root / "visual-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
lines = [f"# {args.release} 视觉差异报告", "", f"- 阈值：{args.threshold:.4%}", f"- 通过：{report['passed']}/{report['total']}", "", "| State ID | 状态 | 差异比例 |", "|---|---|---:|"]
for r in results:
    lines.append(f"| {r['state_id']} | {r['status']} | {r['diff_ratio']:.4%} |")
(out_root / "VISUAL_DIFF_REPORT.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
print(json.dumps(report, ensure_ascii=False, indent=2))
if args.fail_on_diff and failures:
    sys.exit(1)
