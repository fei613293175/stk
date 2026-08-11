param([string]$RepoPath = ".")
$packageRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$repo = (Resolve-Path $RepoPath).Path
$map = Import-Csv (Join-Path $packageRoot 'contracts/INSTALL_FILE_MAP.csv')
foreach ($version in @('V1.3.0','V1.4.0')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $repo "ui-reference/detailed/$version") | Out-Null
}
foreach ($row in $map) {
    $source = Join-Path $packageRoot ("visual/" + $row.file_name)
    $target = Join-Path $repo ("ui-reference/detailed/" + $row.version + "/" + $row.file_name)
    if (-not (Test-Path $source)) { throw "Missing visual file: $source" }
    Copy-Item $source $target -Force
}
$v13 = (Get-ChildItem (Join-Path $repo 'ui-reference/detailed/V1.3.0') -Filter '*.png').Count
$v14 = (Get-ChildItem (Join-Path $repo 'ui-reference/detailed/V1.4.0') -Filter '*.png').Count
if ($v13 -ne 37 -or $v14 -ne 23) { throw "Unexpected visual counts: V1.3.0=$v13, V1.4.0=$v14" }
Write-Host "Visual overlay installed: V1.3.0=$v13, V1.4.0=$v14"
