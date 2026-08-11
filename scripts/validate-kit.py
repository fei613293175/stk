#!/usr/bin/env python3
from pathlib import Path
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []
warnings: list[str] = []

required = [
    '00_START_HERE.md', 'AGENTS.md', 'CURRENT_TASK.md', 'CURRENT_API.md',
    'CURRENT_ACCEPTANCE.md', 'PROJECT_FACTS.json', 'settings.gradle.kts',
    'build.gradle.kts', 'gradlew', 'gradlew.bat',
    'gradle/wrapper/gradle-wrapper.jar',
    'gradle/wrapper/gradle-wrapper.properties',
    'android/app/build.gradle.kts',
    'android/app/src/main/AndroidManifest.xml',
    'discuz/source/plugin/stk_auth/api.inc.php',
    'discuz/source/plugin/stk_project/api.inc.php',
    'discuz/source/plugin/stk_project/install.php',
    '.github/workflows/fast-ci.yml',
]
for item in required:
    if not (ROOT / item).is_file():
        errors.append(f'missing: {item}')

try:
    facts = json.loads((ROOT / 'PROJECT_FACTS.json').read_text(encoding='utf-8'))
    if facts['android']['compile_sdk'] != 36:
        errors.append('compile_sdk must be 36')
    if facts['active_task'] not in {'STK-V100-BETA1', 'STK-V110', 'STK-V120', 'STK-V130', 'STK-V140', 'STK-V141', 'STK-V150'}:
        errors.append('active_task mismatch')
except Exception as exc:
    errors.append(f'PROJECT_FACTS.json invalid: {exc}')

generated_roots = {ROOT / 'build', ROOT / 'android/app/build', ROOT / '.gradle', ROOT / '.kotlin'}
for xml_path in ROOT.rglob('*.xml'):
    if any(root == xml_path or root in xml_path.parents for root in generated_roots):
        continue
    try:
        ET.parse(xml_path)
    except Exception as exc:
        errors.append(f'XML invalid: {xml_path.relative_to(ROOT)}: {exc}')

wrapper_jar = ROOT / 'gradle/wrapper/gradle-wrapper.jar'
expected_wrapper_sha = '81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f'
if wrapper_jar.is_file():
    actual = hashlib.sha256(wrapper_jar.read_bytes()).hexdigest()
    if actual != expected_wrapper_sha:
        errors.append(f'Gradle wrapper JAR SHA mismatch: {actual}')

wrapper_props = ROOT / 'gradle/wrapper/gradle-wrapper.properties'
if wrapper_props.is_file():
    props = wrapper_props.read_text(encoding='utf-8')
    if 'gradle-8.13-bin.zip' not in props:
        errors.append('Gradle wrapper distribution must be 8.13')
    if 'distributionSha256Sum=20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78' not in props:
        errors.append('Gradle distribution SHA lock missing')

for deprecated in ['scripts/gradle.sh', 'scripts/gradle.ps1']:
    if (ROOT / deprecated).exists():
        errors.append(f'deprecated custom downloader remains: {deprecated}')

active_docs = ['AGENTS.md', 'CURRENT_TASK.md', 'CURRENT_ACCEPTANCE.md', 'CODEX_START_PROMPT.md']
for doc in active_docs:
    text = (ROOT / doc).read_text(encoding='utf-8')
    if 'API 37' in text and not any(marker in text for marker in ['不阻断', '不得', '不读取', '不作为']):
        warnings.append(f'check API 37 wording: {doc}')

screen_root = ROOT / 'android/app/src/main/java/com/zzyihao/stk/ui'
for kotlin in screen_root.rglob('*.kt'):
    rel = kotlin.relative_to(ROOT).as_posix()
    if '/theme/' in rel or '/components/' in rel:
        continue
    text = kotlin.read_text(encoding='utf-8')
    for pattern, label in [
        (r'(?<![A-Za-z])\d+(?:\.\d+)?\.dp', 'hard-coded dp'),
        (r'(?<![A-Za-z])\d+(?:\.\d+)?\.sp', 'hard-coded sp'),
        (r'Color\(0x', 'hard-coded color'),
        (r'RoundedCornerShape\(\s*\d', 'hard-coded radius'),
    ]:
        if re.search(pattern, text):
            errors.append(f'{label}: {rel}')

all_text = '\n'.join(
    p.read_text(encoding='utf-8', errors='ignore')
    for p in ROOT.rglob('*')
    if p.is_file() and p.suffix.lower() in {'.kt', '.xml', '.kts', '.java'}
)
if 'android.webkit.WebView' in all_text or re.search(r'<\s*WebView\b', all_text):
    errors.append('internal WebView implementation found')

workflow_files = sorted((ROOT / '.github/workflows').glob('*.yml')) + sorted((ROOT / '.github/workflows').glob('*.yaml'))
if len(workflow_files) != 1 or workflow_files[0].name != 'fast-ci.yml':
    errors.append(f'exactly one active workflow expected, found {[p.name for p in workflow_files]}')

workflow = (ROOT / '.github/workflows/fast-ci.yml').read_text(encoding='utf-8')
for match in re.findall(r'(?:bash|File)\s+([.\\/A-Za-z0-9_-]+)', workflow):
    candidate = match.replace('\\', '/').lstrip('./')
    if candidate.startswith('scripts/') and not (ROOT / candidate).exists():
        errors.append(f'workflow references missing script: {candidate}')
for forbidden in ['emulator', 'avdmanager', 'API 37', 'screenshot', 'pixel']:
    if forbidden.lower() in workflow.lower():
        errors.append(f'Fast CI contains forbidden release gate: {forbidden}')

refs = list((ROOT / 'ui-reference/current').glob('*.png'))
if len(refs) != 10:
    errors.append(f'expected 10 current visual references, found {len(refs)}')

offline_zip = ROOT / 'offline-spec/STK_FULL_SPEC_OFFLINE.zip'
offline_mockups = 0
if offline_zip.is_file():
    try:
        with zipfile.ZipFile(offline_zip) as zf:
            bad = zf.testzip()
            if bad:
                errors.append(f'offline spec ZIP damaged: {bad}')
            offline_mockups = sum(
                1 for name in zf.namelist()
                if name.startswith('SPEC/visual/states/') and name.lower().endswith('.png')
            )
            if offline_mockups != 236:
                errors.append(f'expected 236 offline mockups, found {offline_mockups}')
            if 'SPEC/contracts/visual-reference-manifest.csv' not in zf.namelist():
                errors.append('offline visual manifest missing')
    except Exception as exc:
        errors.append(f'offline spec ZIP invalid: {exc}')
package_paths = [
    p for p in ROOT.rglob('*')
    if not any(root == p or root in p.parents for root in generated_roots)
]
max_path = max((len(str(p.relative_to(ROOT))) for p in package_paths), default=0)
if max_path > 180:
    errors.append(f'package path too long: {max_path}')

report = {
    'status': 'PASS' if not errors else 'FAIL',
    'errors': errors,
    'warnings': warnings,
    'visual_references': len(refs),
    'offline_mockups': offline_mockups,
    'active_workflows': len(workflow_files),
    'max_relative_path_length': max_path,
}
print(json.dumps(report, ensure_ascii=False, indent=2))
sys.exit(1 if errors else 0)
