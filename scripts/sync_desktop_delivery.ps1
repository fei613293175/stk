param(
    [Parameter(Mandatory=$true)][string]$Version,
    [string]$BundlePath,
    [string]$RunId,
    [string]$Repository = "fei613293175/stk"
)
$ErrorActionPreference = "Stop"
$releaseId = "V$Version"
$work = Join-Path $env:TEMP "stk-accepted-$Version"
if (Test-Path $work) { Remove-Item -Recurse -Force $work }
New-Item -ItemType Directory -Force -Path $work | Out-Null

if ($RunId) {
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { throw "缺少 GitHub CLI gh，不能下载指定 CI Artifact" }
    gh run download $RunId --repo $Repository --name "stk-accepted-delivery-*" --dir $work
    $source = $work
} elseif ($BundlePath) {
    $resolved = Resolve-Path $BundlePath
    if ((Get-Item $resolved).PSIsContainer) {
        $source = $resolved.Path
    } else {
        Expand-Archive -Force -Path $resolved.Path -DestinationPath $work
        $source = $work
    }
} else {
    throw "必须提供 -BundlePath 或 -RunId；禁止在本机重新构建 APK 代替 CI Artifact"
}

$required = @(
    "商推客-$Version-release.apk", "FEATURES_PLANNED.md", "FEATURES_COMPLETED.md",
    "OWNER_TEST_CHECKLIST.md", "AUTOMATED_TEST_REPORT.md", "UI_SCREENSHOT_INDEX.csv",
    "VISUAL_DIFF_REPORT.md", "DEPLOYMENT_ENDPOINTS.md", "DNS_ACTION_REQUIRED.md",
    "BUILD_INFO.json", "CI_PROVENANCE.json", "KNOWN_ISSUES.md", "SHA256SUMS.txt"
)
foreach ($name in $required) {
    if (-not (Get-ChildItem -Path $source -Filter $name -File -Recurse)) { throw "Accepted Artifact 缺少 $name" }
}
$buildInfoFile = Get-ChildItem -Path $source -Filter BUILD_INFO.json -File -Recurse | Select-Object -First 1
$build = Get-Content $buildInfoFile.FullName -Raw | ConvertFrom-Json
if ($build.version_name -ne $Version) { throw "BUILD_INFO version_name 不匹配" }
if ($build.application_id -ne "com.zzyihao.stk") { throw "applicationId 不匹配" }
$apk = Get-ChildItem -Path $source -Filter "商推客-$Version-release.apk" -File -Recurse | Select-Object -First 1
$actualSha = (Get-FileHash -Algorithm SHA256 $apk.FullName).Hash.ToLower()
if ($actualSha -ne $build.apk_sha256.ToLower()) { throw "APK SHA 与 BUILD_INFO 不一致" }

$dest = Join-Path $env:USERPROFILE "Desktop\商推客交付\$Version"
if (Test-Path $dest) { Remove-Item -Recurse -Force $dest }
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Get-ChildItem -Path $source -Force | Copy-Item -Destination $dest -Recurse -Force
$receipt = [ordered]@{
    release_id = $releaseId
    version = $Version
    application_id = $build.application_id
    git_commit = $build.git_commit
    github_run_id = $build.github_run_id
    apk_sha256 = $actualSha
    copied_at = (Get-Date).ToString("o")
    source_rule = "Exact accepted GitHub Actions artifact; no local rebuild"
    destination = $dest
}
$receipt | ConvertTo-Json -Depth 4 | Set-Content -Encoding UTF8 (Join-Path $dest "DESKTOP_DELIVERY_RECEIPT.json")
Write-Host "商推客 $Version 已复制到 $dest"
if ($Version -eq "1.0.0") {
    Write-Warning "管理员一次性凭据必须通过本机 ADMIN_ACCESS_HANDOFF.local.md 单独交付，不能存在 Git 或 CI Artifact。"
}
