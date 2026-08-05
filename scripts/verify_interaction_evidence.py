#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ORDER = ["V1.0.0", "V1.1.0", "V1.2.0", "V1.3.0", "V1.4.0"]
p = argparse.ArgumentParser()
p.add_argument("--release", required=True)
p.add_argument("--evidence", required=True)
args = p.parse_args()
if args.release not in ORDER:
    print("未知版本", file=sys.stderr); sys.exit(2)
with (ROOT / "contracts/ui-interaction-map.csv").open(newline="", encoding="utf-8-sig") as f:
    rows = list(csv.DictReader(f))
max_index = ORDER.index(args.release)
required = {r["interaction_id"] for r in rows if ORDER.index(r["introduced_in"]) <= max_index}
evidence = json.loads(Path(args.evidence).read_text(encoding="utf-8"))
passed = set(evidence.get("passed_interaction_ids", []))
failed = set(evidence.get("failed_interaction_ids", []))
missing = sorted(required - passed)
unknown = sorted(passed - {r["interaction_id"] for r in rows})
print(json.dumps({"release": args.release, "required": len(required), "passed": len(passed & required), "failed": sorted(failed), "missing": missing, "unknown": unknown}, ensure_ascii=False, indent=2))
if missing or failed or unknown:
    sys.exit(1)
