#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

auth_manifest_path = ROOT / "source/plugin/stk_auth/discuz_plugin_stk_auth.json"
project_manifest_path = ROOT / "source/plugin/stk_project/discuz_plugin_stk_project.json"
auth_manifest = json.loads(auth_manifest_path.read_text(encoding="utf-8"))["Data"]
project_manifest = json.loads(project_manifest_path.read_text(encoding="utf-8"))["Data"]

expected_auth_pages = {
    "overview": "ADM-AUTH-001", "base": "ADM-AUTH-002", "login": "ADM-AUTH-003",
    "register": "ADM-AUTH-004", "captcha": "ADM-AUTH-005", "sms": "ADM-AUTH-006",
    "sms_records": "ADM-AUTH-007", "users": "ADM-AUTH-008", "login_logs": "ADM-AUTH-009",
    "tokens": "ADM-AUTH-010", "risks": "ADM-AUTH-011", "legal": "ADM-AUTH-012",
    "audit": "ADM-AUTH-013", "diagnostics": "ADM-AUTH-014",
}
expected_project_pages = {
    "overview": "ADM-PROJ-001", "projects": "ADM-PROJ-002", "categories": "ADM-PROJ-003",
    "home": "ADM-PROJ-004", "detail": "ADM-PROJ-005",
}


def module_pages(manifest: dict) -> set[str]:
    modules = manifest["plugin"]["__modules"]
    return {
        module["param"].removeprefix("page=")
        for module in modules
        if module.get("type") == "3" and module.get("name") == "admincp"
    }


assert module_pages(auth_manifest) == set(expected_auth_pages), "stk_auth X5 admin modules do not cover all V1.0 pages"
assert module_pages(project_manifest) == set(expected_project_pages), "stk_project X5 admin modules do not cover all V1.0 pages"

auth_code = (ROOT / "source/plugin/stk_auth/admincp.inc.php").read_text(encoding="utf-8")
project_code = (ROOT / "source/plugin/stk_project/admincp.inc.php").read_text(encoding="utf-8")
for page_id in expected_auth_pages.values():
    assert page_id in auth_code, f"Missing implemented auth admin page {page_id}"
for page_id in expected_project_pages.values():
    assert page_id in project_code, f"Missing implemented project admin page {page_id}"

auth_vars = {item["variable"] for item in auth_manifest["var"]}
project_vars = {item["variable"] for item in project_manifest["var"]}
assert "sms_access_key_id" not in auth_vars and "sms_access_key_secret" not in auth_vars, "SMS secrets must not use plaintext plugin variables"
assert {"admin_role_bindings", "captcha_required_actions", "legal_require_current_version_on_register"} <= auth_vars
assert {"home_page_size", "home_empty_copy", "detail_view_dedupe_seconds"} <= project_vars

support = (ROOT / "source/plugin/stk_auth/admin/AdminSupport.php").read_text(encoding="utf-8")
assert "submitcheck" in support and "stk_admin_audit" in support and "hash_hmac" in support
assert "StkSecretConfig::encrypt" in support
assert "StkSecretConfig::get" in (ROOT / "source/plugin/stk_auth/integration/aliyun_sms.php").read_text(encoding="utf-8")

assert not (ROOT / "source/plugin/stk_auth/discuz_plugin_stk_auth.xml").exists()
assert not (ROOT / "source/plugin/stk_project/discuz_plugin_stk_project.xml").exists()
print("admin_contract_test: PASS")
