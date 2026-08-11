$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Source = Join-Path $Root "deliveries\1.5.0"
if (-not (Test-Path $Source)) {
    throw "交付目录不存在，请先运行 scripts\build-beta-apk.ps1：$Source"
}
$Desktop = [Environment]::GetFolderPath([Environment+SpecialFolder]::Desktop)
$Target = Join-Path $Desktop "商推客交付\1.5.0"
New-Item -ItemType Directory -Force -Path $Target | Out-Null
Copy-Item -Path (Join-Path $Source "*") -Destination $Target -Recurse -Force
Write-Host "已复制到：$Target"
