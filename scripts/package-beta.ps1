$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Source = Join-Path $Root "android\app\build\outputs\apk\release\app-release.apk"
$Delivery = Join-Path $Root "deliveries\1.0.0-beta.2"
if (-not (Test-Path $Source)) { throw "APK 不存在：$Source" }
New-Item -ItemType Directory -Force -Path $Delivery | Out-Null
$Target = Join-Path $Delivery "STK-1.0.0-beta.2.apk"
Copy-Item -Force $Source $Target
(Get-FileHash -Algorithm SHA256 $Target).Hash.ToLowerInvariant() | Set-Content -Encoding ascii (Join-Path $Delivery "APK_SHA256.txt")
$Commit = "UNCOMMITTED_SCAFFOLD"
$Git = Get-Command git -ErrorAction SilentlyContinue
if ($Git -and (Test-Path (Join-Path $Root ".git"))) {
    $ResolvedCommit = & $Git.Source -C $Root rev-parse HEAD 2>$null
    if ($LASTEXITCODE -eq 0 -and $ResolvedCommit) {
        $Commit = $ResolvedCommit.Trim()
    }
}
$Commit | Set-Content -Encoding ascii (Join-Path $Delivery "COMMIT.txt")
Copy-Item -Force (Join-Path $Root "templates\FUNCTION_COMPLETION.md") (Join-Path $Delivery "FUNCTION_COMPLETION.md")
Copy-Item -Force (Join-Path $Root "templates\KNOWN_ISSUES.md") (Join-Path $Delivery "KNOWN_ISSUES.md")
Copy-Item -Force (Join-Path $Root "templates\OWNER_TEST_GUIDE.md") (Join-Path $Delivery "OWNER_TEST_GUIDE.md")
Write-Host "DELIVERED: $Delivery"
exit 0
