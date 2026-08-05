#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--release")
p.add_argument("--check-only", action="store_true")
p.add_argument("--output", default="artifacts/mockup-jobs")
args = p.parse_args()

with (ROOT / "contracts/mockup-batch-plan.csv").open(newline="", encoding="utf-8-sig") as f:
    batches = list(csv.DictReader(f))
with (ROOT / "contracts/mockup-manifest.csv").open(newline="", encoding="utf-8-sig") as f:
    mockups = list(csv.DictReader(f))
if args.release:
    batches = [b for b in batches if b["release_id"] == args.release]
    mockups = [m for m in mockups if m["release_id"] == args.release]
by_id = {m["mockup_id"]: m for m in mockups}
errors = []
jobs = []
for b in batches:
    ids = [x for x in b["mockup_ids"].split(";") if x]
    if int(b["mockup_count"]) != len(ids):
        errors.append(f"批次数量不符: {b['batch_id']}")
    missing = [x for x in ids if x not in by_id]
    if missing:
        errors.append(f"批次引用不存在效果图: {b['batch_id']} {missing}")
    jobs.append({
        "batch_id": b["batch_id"], "release_id": b["release_id"], "domain": b["domain"],
        "generation_order": int(b["generation_order"]), "rules": b["rule"],
        "design_tokens": "contracts/ui-design-tokens.yaml", "fixture_data": "ui/FIXTURE_DATA.yaml",
        "items": [by_id[x] for x in ids if x in by_id],
    })
covered = {item["mockup_id"] for job in jobs for item in job["items"]}
expected = {m["mockup_id"] for m in mockups}
if covered != expected:
    errors.append(f"批次覆盖不完整: missing={sorted(expected-covered)}, extra={sorted(covered-expected)}")
if not args.check_only:
    out = ROOT / args.output
    out.mkdir(parents=True, exist_ok=True)
    for job in jobs:
        (out / f"{job['batch_id']}.json").write_text(json.dumps(job, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps({"jobs": len(jobs), "mockups": len(mockups), "errors": errors}, ensure_ascii=False, indent=2))
if errors:
    sys.exit(1)
