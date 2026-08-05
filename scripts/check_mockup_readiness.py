#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

p = argparse.ArgumentParser()
g = p.add_mutually_exclusive_group(required=True)
g.add_argument("--release")
g.add_argument("--all", action="store_true")
p.add_argument("--allow-planned", action="store_true", help="只验证矩阵完整性；不把 PLANNED 当作已批准")
args = p.parse_args()

with (ROOT / "contracts/mockup-manifest.csv").open(newline="", encoding="utf-8-sig") as f:
    rows = list(csv.DictReader(f))
if args.release:
    rows = [r for r in rows if r["release_id"] == args.release]
if not rows:
    print("没有匹配的效果图合同", file=sys.stderr)
    sys.exit(2)

errors = []
counts = Counter(r["status"] for r in rows)
allowed = {"PLANNED", "GENERATED", "IN_REVIEW", "APPROVED", "REJECTED"}
for r in rows:
    if r["status"] not in allowed:
        errors.append(f"非法状态 {r['mockup_id']}: {r['status']}")
    if r["status"] == "APPROVED":
        fp = ROOT / r["file_path"]
        if not fp.is_file():
            errors.append(f"批准文件不存在: {r['mockup_id']} -> {r['file_path']}")
            continue
        actual = hashlib.sha256(fp.read_bytes()).hexdigest()
        if actual.lower() != r["sha256"].lower():
            errors.append(f"SHA 不匹配: {r['mockup_id']}")
        if r["approved_by"] in {"", "NOT_APPROVED"} or r["approved_at"] in {"", "NOT_APPROVED"}:
            errors.append(f"批准人/时间缺失: {r['mockup_id']}")
    elif not args.allow_planned:
        errors.append(f"尚未批准: {r['mockup_id']} [{r['status']}]")

print(json.dumps({"release": args.release or "ALL", "count": len(rows), "status_counts": counts, "errors": errors}, ensure_ascii=False, indent=2, default=dict))
if errors:
    sys.exit(1)
