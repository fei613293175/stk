#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--operation", required=True)
p.add_argument("--base-url")
p.add_argument("--fixture", help="真实请求 fixture JSON；live 模式必填")
args = p.parse_args()
with (ROOT / "contracts/api-inventory.csv").open(newline="", encoding="utf-8-sig") as f:
    operations = {r["operation_id"]: r for r in csv.DictReader(f)}
if args.operation not in operations:
    print("operationId 不存在", file=sys.stderr)
    sys.exit(2)
op = operations[args.operation]
print(json.dumps(op, ensure_ascii=False, indent=2))
if not args.base_url:
    sys.exit(0)
if not args.fixture:
    print("live 模式必须提供明确 fixture，禁止用猜测参数调用生产接口", file=sys.stderr)
    sys.exit(2)
fixture = json.loads(Path(args.fixture).read_text(encoding="utf-8"))
url = args.base_url.rstrip("/") + fixture.get("path", op["path"])
body = None if op["method"] == "GET" else json.dumps(fixture.get("body", {})).encode("utf-8")
headers = {"Accept": "application/json", "Content-Type": "application/json", "X-STK-Test": "contract"}
token = os.getenv("STK_TEST_ACCESS_TOKEN")
if token:
    headers["Authorization"] = f"Bearer {token}"
req = urllib.request.Request(url, data=body, headers=headers, method=op["method"])
try:
    with urllib.request.urlopen(req, timeout=20) as resp:
        payload = json.loads(resp.read().decode("utf-8"))
        if not {"code", "message", "data", "request_id", "server_time"}.issubset(payload):
            raise RuntimeError("响应不符合统一 Envelope")
        print(json.dumps(payload, ensure_ascii=False, indent=2))
except urllib.error.HTTPError as exc:
    payload = json.loads(exc.read().decode("utf-8"))
    if "code" not in payload or "request_id" not in payload:
        raise
    print(json.dumps(payload, ensure_ascii=False, indent=2))
