param(
    [Parameter(Mandatory=$false)]
    [string]$RepoPath = "."
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PackRoot = Split-Path -Parent $ScriptDir
$Repo = (Resolve-Path $RepoPath).Path
$Target = Join-Path $Repo "ui-reference\detailed\V1.1.0"
$ContractTarget = Join-Path $Repo "ui-reference\detailed\contracts\V1.1.0"

New-Item -ItemType Directory -Force -Path $Target | Out-Null
New-Item -ItemType Directory -Force -Path $ContractTarget | Out-Null
Copy-Item -Force (Join-Path $PackRoot "visual\*.png") $Target
Copy-Item -Force (Join-Path $PackRoot "contracts\*.csv") $ContractTarget
Copy-Item -Force (Join-Path $PackRoot "VISUAL_BATCHES_V1.1.0.csv") (Join-Path $Repo "VISUAL_BATCHES_V1.1.0.csv")
Copy-Item -Force (Join-Path $PackRoot "VISUAL_IMPLEMENTATION_STATUS_V1.1.0.csv") (Join-Path $Repo "VISUAL_IMPLEMENTATION_STATUS_V1.1.0.csv")
Copy-Item -Force (Join-Path $PackRoot "CURRENT_VISUAL_TASK_V1.1.0.md") (Join-Path $Repo "CURRENT_VISUAL_TASK_V1.1.0.md")
Copy-Item -Force (Join-Path $PackRoot "PAGE_VISUAL_CHECKLIST_V1.1.0.md") (Join-Path $Repo "PAGE_VISUAL_CHECKLIST_V1.1.0.md")

$count = (Get-ChildItem -File $Target -Filter "*.png").Count
if ($count -ne 45) { throw "Expected 45 PNG files, found $count" }
Write-Host "Installed 45 V1.1.0 detailed visual references into $Target"
Write-Host "Version-specific task files were installed without overwriting V1.0.0 records."
Write-Host "No business code, AGENTS.md, CI workflow or build configuration was overwritten."
