#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--entity")
args = p.parse_args()
entities = yaml.safe_load((ROOT / "contracts/database-entity-catalog.yaml").read_text(encoding="utf-8"))["entities"]
if args.entity:
    entities = [e for e in entities if e["entity_id"] == args.entity]
    if not entities:
        print("未知 Entity ID", file=sys.stderr)
        sys.exit(2)
sql_files = sorted((ROOT / "backend/sql").glob("*.sql"))
sql = "\n".join(p.read_text(encoding="utf-8") for p in sql_files)
errors = []
for e in entities:
    if f"CREATE TABLE IF NOT EXISTS {e['table']}" not in sql and f"ALTER TABLE {e['table']}" not in sql:
        errors.append(f"{e['entity_id']} 没有 DDL: {e['table']}")
for forbidden in ["DROP TABLE", "TRUNCATE TABLE", "mobile VARCHAR", "code VARCHAR(6)"]:
    if forbidden.lower() in sql.lower():
        errors.append(f"DDL 出现禁止内容: {forbidden}")
print(f"checked_entities={len(entities)} sql_files={len(sql_files)}")
if errors:
    print("\n".join(errors), file=sys.stderr)
    sys.exit(1)
