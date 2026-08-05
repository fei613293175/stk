#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--mockup-id", required=True)
p.add_argument("--image", required=True)
p.add_argument("--reviewer", required=True)
p.add_argument("--approve", action="store_true")
args = p.parse_args()
manifest = ROOT / "contracts/mockup-manifest.csv"
with manifest.open(newline="", encoding="utf-8-sig") as f:
    rows = list(csv.DictReader(f)); fields = f.fieldnames
matches = [r for r in rows if r["mockup_id"] == args.mockup_id]
if len(matches) != 1:
    print("Mockup ID 不唯一或不存在", file=sys.stderr); sys.exit(2)
r = matches[0]
src = Path(args.image)
if not src.is_file(): print("图片不存在", file=sys.stderr); sys.exit(2)
from PIL import Image
with Image.open(src) as im:
    if im.size != (1170, 2532):
        print(f"图片尺寸必须为 1170x2532，实际 {im.size}", file=sys.stderr); sys.exit(1)
status = "APPROVED" if args.approve else "GENERATED"
root = "approved" if args.approve else "generated"
dest = ROOT / "ui/mockups" / root / r["release_id"] / r["page_id"] / src.name
if dest.exists(): print("目标文件已存在，禁止静默覆盖", file=sys.stderr); sys.exit(1)
dest.parent.mkdir(parents=True, exist_ok=True)
shutil.copy2(src, dest)
r["file_path"] = dest.relative_to(ROOT).as_posix()
r["sha256"] = hashlib.sha256(dest.read_bytes()).hexdigest()
r["status"] = status
r["approved_by"] = args.reviewer if args.approve else "NOT_APPROVED"
r["approved_at"] = datetime.now(timezone.utc).isoformat() if args.approve else "NOT_APPROVED"
with manifest.open("w", newline="", encoding="utf-8-sig") as f:
    w = csv.DictWriter(f, fieldnames=fields); w.writeheader(); w.writerows(rows)
print(f"{args.mockup_id} -> {status}: {r['file_path']}")
