#!/usr/bin/env python3
from __future__ import annotations

import csv
import json
import re
import sys
from collections import Counter
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[1]
VERSIONS = ["V1.0.0", "V1.1.0", "V1.2.0", "V1.3.0", "V1.4.0"]
TEXT_SUFFIXES = {".md", ".yaml", ".yml", ".csv", ".json", ".sql", ".py", ".sh", ".ps1", ".txt"}
PINNED_ANDROID_API = 36
EXPLICITLY_FORBIDDEN_ANDROID_APIS = [37]


def load_yaml(rel: str):
    return yaml.safe_load((ROOT / rel).read_text(encoding="utf-8"))


def load_csv(rel: str):
    with (ROOT / rel).open(newline="", encoding="utf-8-sig") as f:
        return list(csv.DictReader(f))


def duplicate_values(rows, key):
    c = Counter(row[key] for row in rows)
    return sorted(v for v, count in c.items() if count > 1)


def split_refs(value):
    if value is None:
        return []
    if isinstance(value, list):
        return [str(v) for v in value if str(v)]
    return [x for x in str(value).split(";") if x and not x.startswith("NONE")]


errors: list[str] = []
warnings: list[str] = []

required = [
    "00_READ_ME_FIRST.md", "AGENTS.md", "PROJECT_CONTEXT.yaml", "CURRENT_RELEASE.yaml",
    "contracts/ui-design-tokens.yaml", "contracts/ui-page-catalog.yaml", "contracts/ui-state-catalog.csv",
    "contracts/mockup-manifest.csv", "contracts/ui-interaction-map.csv", "contracts/api-inventory.csv",
    "contracts/database-entity-catalog.yaml", "contracts/admin-menu-map.yaml", "contracts/config-inventory.csv",
    "contracts/feature-map.yaml", "contracts/test-catalog.csv", "contracts/frontend-backend-traceability.csv",
    "contracts/repository-policy.yaml", "contracts/domain-delivery-map.yaml", "contracts/owner-delivery-contract.yaml",
    "backend/DISCUZ_PLUGIN_IMPLEMENTATION.md", "scripts/check_mockup_readiness.py",
    ".github/workflows/contract-validation.yml",
]
for rel in required:
    if not (ROOT / rel).exists():
        errors.append(f"缺少必需文件: {rel}")

context = load_yaml("PROJECT_CONTEXT.yaml")
if context["domains"]["registrable_domain"] != "zz-yihao.com":
    errors.append("主域名必须精确为 zz-yihao.com")
if context["repository"]["url"] != "https://github.com/fei613293175/stk.git":
    errors.append("仓库地址不匹配固定仓库")
if context["android"]["application_id"] != "com.zzyihao.stk":
    errors.append("Android applicationId 漂移")
if context["android"]["compile_sdk"] != PINNED_ANDROID_API:
    errors.append(f"PROJECT_CONTEXT compileSdk 必须固定为 API {PINNED_ANDROID_API}")
if context["android"]["target_sdk"] != PINNED_ANDROID_API:
    errors.append(f"PROJECT_CONTEXT targetSdk 必须固定为 API {PINNED_ANDROID_API}")
if context["android"].get("api_level") != PINNED_ANDROID_API:
    errors.append(f"PROJECT_CONTEXT api_level 必须固定为 API {PINNED_ANDROID_API}")
if context["android"].get("api_channel") != "stable":
    errors.append("PROJECT_CONTEXT api_channel 必须固定为 stable")
if context["android"].get("forbidden_api_levels") != "all_except_36":
    errors.append("PROJECT_CONTEXT 必须禁止所有非 API 36 平台")
if context["android"].get("explicitly_forbidden_api_levels") != EXPLICITLY_FORBIDDEN_ANDROID_APIS:
    errors.append("PROJECT_CONTEXT 必须显式禁止 API 37")
emulator_gate = context["execution_environment_policy"]["emulator_acceptance_gate"]
if emulator_gate["api_level"] != PINNED_ANDROID_API:
    errors.append(f"PROJECT_CONTEXT 模拟器必须固定为 API {PINNED_ANDROID_API}")
if emulator_gate["execution_preference"] != "github_actions_only":
    errors.append("PROJECT_CONTEXT 模拟器验收必须仅允许 GitHub Actions")

for gradle_file in (ROOT / "android-app").rglob("*.gradle.kts"):
    gradle_text = gradle_file.read_text(encoding="utf-8")
    for field in ("compileSdk", "targetSdk"):
        for value in re.findall(rf"\b{field}\s*=\s*(\d+)", gradle_text):
            if int(value) != PINNED_ANDROID_API:
                errors.append(f"{gradle_file.relative_to(ROOT)} {field} 必须固定为 API {PINNED_ANDROID_API}")

for workflow_rel in [
    ".github/workflows/android-emulator-visual.yml",
    ".github/workflows/release-acceptance.yml",
]:
    workflow_text = (ROOT / workflow_rel).read_text(encoding="utf-8")
    api_levels = re.findall(r"api-level:\s*['\"]?([0-9]+(?:\.[0-9]+)?)", workflow_text)
    if api_levels != [str(PINNED_ANDROID_API)]:
        errors.append(f"{workflow_rel} 必须且只能配置 api-level: '{PINNED_ANDROID_API}'")
    channels = re.findall(r"^\s*channel:\s*([A-Za-z]+)\s*$", workflow_text, flags=re.MULTILINE)
    if channels != ["stable"]:
        errors.append(f"{workflow_rel} 必须且只能使用 stable Android channel")

wrong_domain = "orbe" + "xa.cc"
forbidden_android_markers = (
    f"Android SDK {PINNED_ANDROID_API + 1}",
    f"platforms;android-{PINNED_ANDROID_API + 1}",
    f"system-images;android-{PINNED_ANDROID_API + 1}",
    f"api-level: '{PINNED_ANDROID_API + 1}'",
    f'api-level: "{PINNED_ANDROID_API + 1}"',
    f"compileSdk = {PINNED_ANDROID_API + 1}",
    f"targetSdk = {PINNED_ANDROID_API + 1}",
    f"compile_sdk: {PINNED_ANDROID_API + 1}",
    f"target_sdk: {PINNED_ANDROID_API + 1}",
    f"allowed_api_level: {PINNED_ANDROID_API + 1}",
    f"stable_api_level: {PINNED_ANDROID_API + 1}",
)
for p in ROOT.rglob("*"):
    if any(part in {".git", "artifacts"} for part in p.parts):
        continue
    if p.is_file() and p.suffix.lower() in TEXT_SUFFIXES:
        try:
            text = p.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if wrong_domain.lower() in text.lower():
            errors.append(f"发现已废弃错误域名: {p.relative_to(ROOT)}")
        for marker in forbidden_android_markers:
            if marker in text:
                errors.append(f"发现禁止的 Android 平台标记 {marker}: {p.relative_to(ROOT)}")

pages = load_yaml("contracts/ui-page-catalog.yaml")["pages"]
states = load_csv("contracts/ui-state-catalog.csv")
mockups = load_csv("contracts/mockup-manifest.csv")
interactions = load_csv("contracts/ui-interaction-map.csv")
apis = load_csv("contracts/api-inventory.csv")
dbs = load_yaml("contracts/database-entity-catalog.yaml")["entities"]
admins = load_yaml("contracts/admin-menu-map.yaml")["menus"]
configs = load_csv("contracts/config-inventory.csv")
features = load_yaml("contracts/feature-map.yaml")["features"]
tests = load_csv("contracts/test-catalog.csv")
trace = load_csv("contracts/frontend-backend-traceability.csv")
bindings = load_csv("contracts/interaction-test-bindings.csv")

collections = [
    (pages, "page_id", "Page ID"), (states, "state_id", "State ID"), (mockups, "mockup_id", "Mockup ID"),
    (interactions, "interaction_id", "Interaction ID"), (apis, "operation_id", "API operationId"),
    (dbs, "entity_id", "DB Entity ID"), (admins, "admin_menu_id", "Admin Menu ID"),
    (configs, "config_id", "Config ID"), (features, "id", "Feature ID"), (tests, "test_id", "Test ID"),
]
for rows, key, label in collections:
    dups = duplicate_values(rows, key)
    if dups:
        errors.append(f"{label} 重复: {dups[:10]}")

page_ids = {p["page_id"] for p in pages}
state_ids = {s["state_id"] for s in states}
feature_ids = {f["id"] for f in features}
api_ids = {a["operation_id"] for a in apis}
db_ids = {d["entity_id"] for d in dbs}
admin_ids = {a["admin_menu_id"] for a in admins}
config_ids = {c["config_id"] for c in configs}
test_ids = {t["test_id"] for t in tests}
interaction_ids = {i["interaction_id"] for i in interactions}

for s in states:
    if s["page_id"] not in page_ids:
        errors.append(f"状态引用不存在页面: {s['state_id']} -> {s['page_id']}")
    if s["introduced_in"] not in VERSIONS:
        errors.append(f"状态版本无效: {s['state_id']} -> {s['introduced_in']}")
for pid in page_ids:
    if not any(s["page_id"] == pid for s in states):
        errors.append(f"页面没有任何状态: {pid}")

mockup_by_state = Counter(m["state_id"] for m in mockups)
for sid in state_ids:
    if mockup_by_state[sid] != 1:
        errors.append(f"每个状态必须精确绑定一张独立效果图: {sid}, count={mockup_by_state[sid]}")
for m in mockups:
    if m["state_id"] not in state_ids or m["page_id"] not in page_ids:
        errors.append(f"效果图引用错误: {m['mockup_id']}")
    if m["status"] == "APPROVED":
        fp = ROOT / m["file_path"]
        if not fp.is_file():
            errors.append(f"已批准效果图文件不存在: {m['mockup_id']} -> {m['file_path']}")
        if m["sha256"] in {"", "NOT_GENERATED", "NOT_APPROVED"}:
            errors.append(f"已批准效果图缺 SHA-256: {m['mockup_id']}")

seen_tags = Counter(i["test_tag"] for i in interactions)
for tag, count in seen_tags.items():
    if count > 1:
        errors.append(f"testTag 必须全局唯一: {tag} x{count}")
for i in interactions:
    if i["page_id"] not in page_ids:
        errors.append(f"交互引用不存在页面: {i['interaction_id']}")
    if i["feature_id"] not in feature_ids:
        errors.append(f"交互引用不存在 Feature: {i['interaction_id']} -> {i['feature_id']}")

binding_ids = Counter(b["interaction_id"] for b in bindings)
for iid in interaction_ids:
    if binding_ids[iid] != 1:
        errors.append(f"每个交互必须精确绑定一个 UI 自动测试: {iid}")
for b in bindings:
    if b["test_id"] not in test_ids:
        errors.append(f"交互测试绑定引用不存在测试: {b['interaction_id']} -> {b['test_id']}")

for a in apis:
    if a["feature_id"] not in feature_ids:
        errors.append(f"API 引用不存在 Feature: {a['operation_id']} -> {a['feature_id']}")
    if a["introduced_in"] not in VERSIONS:
        errors.append(f"API 版本无效: {a['operation_id']}")
for d in dbs:
    if d["version"] not in VERSIONS:
        errors.append(f"数据实体版本无效: {d['entity_id']}")
for c in configs:
    if c["admin_menu_id"] not in admin_ids:
        errors.append(f"配置引用不存在后台页面: {c['config_id']} -> {c['admin_menu_id']}")

for f in features:
    refs = {
        "page_ids": page_ids, "state_ids": state_ids, "interaction_ids": interaction_ids,
        "api_operation_ids": api_ids, "entity_ids": db_ids, "admin_menu_ids": admin_ids,
        "config_ids": config_ids, "test_ids": test_ids,
    }
    for field, valid in refs.items():
        for ref in split_refs(f.get(field, [])):
            if ref not in valid:
                errors.append(f"Feature {f['id']} 的 {field} 引用不存在: {ref}")
    if f["release_id"] not in VERSIONS:
        errors.append(f"Feature 版本无效: {f['id']}")
    else:
        feature_index = VERSIONS.index(f["release_id"])
        introduced_maps = {
            "api_operation_ids": {x["operation_id"]: x["introduced_in"] for x in apis},
            "entity_ids": {x["entity_id"]: x["version"] for x in dbs},
            "admin_menu_ids": {x["admin_menu_id"]: x["introduced_in"] for x in admins},
            "config_ids": {x["config_id"]: x["introduced_in"] for x in configs},
        }
        for field, introduced in introduced_maps.items():
            for ref in split_refs(f.get(field, [])):
                if ref in introduced and VERSIONS.index(introduced[ref]) > feature_index:
                    errors.append(f"Feature {f['id']} 提前依赖后续版本 {field}: {ref} -> {introduced[ref]}")

mapped_sets = {
    "pages": {x for f in features for x in split_refs(f.get("page_ids", []))},
    "states": {x for f in features for x in split_refs(f.get("state_ids", []))},
    "interactions": {x for f in features for x in split_refs(f.get("interaction_ids", []))},
    "apis": {x for f in features for x in split_refs(f.get("api_operation_ids", []))},
    "database_entities": {x for f in features for x in split_refs(f.get("entity_ids", []))},
    "admin_pages": {x for f in features for x in split_refs(f.get("admin_menu_ids", []))},
    "configs": {x for f in features for x in split_refs(f.get("config_ids", []))},
}
all_sets = {
    "pages": page_ids, "states": state_ids, "interactions": interaction_ids, "apis": api_ids,
    "database_entities": db_ids, "admin_pages": admin_ids, "configs": config_ids,
}
for label, all_values in all_sets.items():
    missing = sorted(all_values - mapped_sets[label])
    if missing:
        errors.append(f"存在未绑定任何 Feature 的 {label}: {missing}")

if len(trace) != len(features):
    errors.append(f"前后端追踪矩阵必须每个 Feature 一行: feature={len(features)}, trace={len(trace)}")

for vid in VERSIONS:
    vdir = ROOT / "versions" / vid
    required_version_files = [
        "VERSION_DEVELOPMENT_SPEC.md", "UI_FIXED_RULES.md", "FRONTEND_SCOPE.md",
        "BACKEND_ADMIN_SCOPE.md", "PAGE_STATE_MOCKUP_BINDINGS.csv", "FEATURE_TRACEABILITY.csv",
        "TEST_AND_OWNER_ACCEPTANCE.md", "DELIVERY_CHECKLIST.md", "CODEX_START_PROMPT.md",
        "ENVIRONMENT_CONTRACT.yaml",
    ]
    for name in required_version_files:
        if not (vdir / name).exists():
            errors.append(f"{vid} 缺少文件: {name}")
    ui_rules = (vdir / "UI_FIXED_RULES.md").read_text(encoding="utf-8") if (vdir / "UI_FIXED_RULES.md").exists() else ""
    for fixed in ["STK-DS-1.0", "#246BFD", "56dp", "64dp", "48dp", "16dp", "390×844"]:
        if fixed not in ui_rules:
            errors.append(f"{vid} 未重复固定 UI 参数: {fixed}")
    environment_contract = load_yaml(f"versions/{vid}/ENVIRONMENT_CONTRACT.yaml")
    contract_pins = environment_contract["contract_pins"]
    for field in ("compile_sdk", "target_sdk", "allowed_api_level"):
        if contract_pins.get(field) != PINNED_ANDROID_API:
            errors.append(f"{vid} {field} 必须固定为 API {PINNED_ANDROID_API}")
    if contract_pins.get("api_channel") != "stable":
        errors.append(f"{vid} api_channel 必须固定为 stable")
    if contract_pins.get("stable_api_level") != PINNED_ANDROID_API:
        errors.append(f"{vid} stable_api_level 必须固定为 API {PINNED_ANDROID_API}")
    if contract_pins.get("forbidden_api_levels") != "all_except_36":
        errors.append(f"{vid} 必须禁止所有非 API {PINNED_ANDROID_API} 平台")
    if contract_pins.get("explicitly_forbidden_api_levels") != EXPLICITLY_FORBIDDEN_ANDROID_APIS:
        errors.append(f"{vid} 必须显式禁止 API 37")
    version_emulator_gate = environment_contract["gates"]["emulator_acceptance"]
    if version_emulator_gate.get("api_level") != PINNED_ANDROID_API:
        errors.append(f"{vid} 模拟器必须固定为 API {PINNED_ANDROID_API}")
    if version_emulator_gate.get("execution") != "github_actions_only":
        errors.append(f"{vid} 模拟器验收必须仅允许 GitHub Actions")

repo = load_yaml("contracts/repository-policy.yaml")
if repo["remote_url"] != "https://github.com/fei613293175/stk.git":
    errors.append("repository-policy 仓库地址不正确")
domain = load_yaml("contracts/domain-delivery-map.yaml")
if domain["registrable_domain"] != "zz-yihao.com":
    errors.append("domain-delivery-map 主域名不正确")
if not any(d["host"] == "stk-admin.zz-yihao.com" and d["service_required_in"] == "V1.0.0" for d in domain["domains"]):
    errors.append("V1.0.0 后台域名/交付规则缺失")

sql_text = "\n".join(p.read_text(encoding="utf-8") for p in (ROOT / "backend/sql").glob("*.sql"))
for d in dbs:
    if d["table"] not in sql_text:
        errors.append(f"数据库实体没有 DDL: {d['entity_id']} {d['table']}")

for rel in [
    "scripts/check_mockup_readiness.py", "scripts/generate_mockup_jobs.py", "scripts/compare_visuals.py",
    "scripts/verify_release_artifact.py", "scripts/create_release_bundle.py", "scripts/sync_desktop_delivery.ps1",
    "scripts/ci/run_emulator_acceptance.sh", "scripts/ci/run_exact_release_acceptance.sh",
]:
    if not (ROOT / rel).exists():
        errors.append(f"合同引用脚本不存在: {rel}")

print(json.dumps({
    "root": str(ROOT), "pages": len(pages), "states": len(states), "mockups": len(mockups),
    "interactions": len(interactions), "apis": len(apis), "database_entities": len(dbs),
    "admin_pages": len(admins), "configs": len(configs), "features": len(features), "tests": len(tests),
    "warnings": warnings, "errors": errors,
}, ensure_ascii=False, indent=2))
if errors:
    sys.exit(1)
