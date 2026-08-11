#!/usr/bin/env python3
from pathlib import Path
import hashlib
import json
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'PACKAGE_MANIFEST.json'
SUMS = ROOT / 'SHA256SUMS.txt'
EXCLUDE_FROM_MANIFEST = {'PACKAGE_MANIFEST.json', 'SHA256SUMS.txt'}
EXCLUDE_FROM_SUMS = {'SHA256SUMS.txt'}
GENERATED_ROOTS = {'build', 'android/app/build', '.gradle', '.kotlin', '.tools', 'deliveries'}

def is_generated(rel: str) -> bool:
    return any(rel == root or rel.startswith(root + '/') for root in GENERATED_ROOTS)


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()

files = []
for path in sorted(ROOT.rglob('*')):
    if not path.is_file():
        continue
    rel = path.relative_to(ROOT).as_posix()
    if rel in EXCLUDE_FROM_MANIFEST or is_generated(rel):
        continue
    files.append({'path': rel, 'size': path.stat().st_size, 'sha256': sha256(path)})

payload = {
    'schema': '1.0.0',
    'package': 'STK_FAST_EXECUTION_V1_5_0',
    'generated_at_utc': datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
    'active_task': 'STK-V150',
    'file_count_excluding_metadata': len(files),
    'files': files,
}
MANIFEST.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

sum_lines = []
for path in sorted(ROOT.rglob('*')):
    if not path.is_file():
        continue
    rel = path.relative_to(ROOT).as_posix()
    if rel in EXCLUDE_FROM_SUMS or is_generated(rel):
        continue
    sum_lines.append(f'{sha256(path)}  {rel}')
SUMS.write_text('\n'.join(sum_lines) + '\n', encoding='utf-8')
print(json.dumps({'manifest_files': len(files), 'sha_entries': len(sum_lines)}, ensure_ascii=False))
