param(
    [Parameter(Mandatory=$false)]
    [string]$RepoPath = "."
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PackRoot = Split-Path -Parent $ScriptDir
$Repo = (Resolve-Path $RepoPath).Path
$Target = Join-Path $Repo "ui-reference\detailed\V1.0.0"
$ContractTarget = Join-Path $Repo "ui-reference\detailed\contracts"

New-Item -ItemType Directory -Force -Path $Target | Out-Null
New-Item -ItemType Directory -Force -Path $ContractTarget | Out-Null
Copy-Item -Force (Join-Path $PackRoot "visual\*.png") $Target
Copy-Item -Force (Join-Path $PackRoot "contracts\*.csv") $ContractTarget
Copy-Item -Force (Join-Path $PackRoot "VISUAL_BATCHES.csv") (Join-Path $Repo "VISUAL_BATCHES.csv")
Copy-Item -Force (Join-Path $PackRoot "VISUAL_IMPLEMENTATION_STATUS.csv") (Join-Path $Repo "VISUAL_IMPLEMENTATION_STATUS.csv")
Copy-Item -Force (Join-Path $PackRoot "CURRENT_VISUAL_TASK.md") (Join-Path $Repo "CURRENT_VISUAL_TASK.md")
Copy-Item -Force (Join-Path $PackRoot "PAGE_VISUAL_CHECKLIST.md") (Join-Path $Repo "PAGE_VISUAL_CHECKLIST.md")

$count = (Get-ChildItem -File $Target -Filter "*.png").Count
if ($count -ne 67) { throw "Expected 67 PNG files, found $count" }
Write-Host "Installed 67 V1.0.0 detailed visual references into $Target"
Write-Host "No business code, AGENTS.md, CI workflow or build configuration was overwritten."
