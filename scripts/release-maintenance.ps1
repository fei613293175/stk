param(
    [ValidateSet('enable','disable','status','backup','rollback')][string]$Action='status',
    [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [string]$BackupPath='',
    [switch]$DryRun,
    [switch]$ConfirmRollback
)

$ErrorActionPreference = 'Stop'
$ReleasePath = Join-Path $Root 'release'
$BackupRoot = Join-Path $Root 'release-backups'
$State = Join-Path $ReleasePath 'maintenance.flag'

function Write-Plan([string]$Message) {
    if ($DryRun) { Write-Output "DRY_RUN: $Message" } else { Write-Output $Message }
}

function Set-Maintenance([bool]$Enabled) {
    $updatePath = Join-Path $ReleasePath 'update.json'
    if (-not (Test-Path -LiteralPath $updatePath)) { throw "更新清单不存在：$updatePath" }
    if ($DryRun) { Write-Plan "set maintenance.enabled=$Enabled in $updatePath"; return }
    $json = Get-Content -LiteralPath $updatePath -Raw | ConvertFrom-Json
    if (-not $json.maintenance) {
        $json | Add-Member -NotePropertyName maintenance -NotePropertyValue ([pscustomobject]@{
            enabled = $Enabled; message = '服务维护中，请稍后再试。'; resume_at = $null
        })
    } else {
        $json.maintenance.enabled = $Enabled
        if ([string]::IsNullOrWhiteSpace([string]$json.maintenance.message)) { $json.maintenance.message = '服务维护中，请稍后再试。' }
    }
    $content = $json | ConvertTo-Json -Depth 8
    [System.IO.File]::WriteAllText($updatePath, $content, (New-Object System.Text.UTF8Encoding($false)))
    if ($Enabled) { 'maintenance=1' | Set-Content -LiteralPath $State -Encoding ascii }
    elseif (Test-Path -LiteralPath $State) { Remove-Item -LiteralPath $State -Force }
}

function New-ReleaseBackup {
    $stamp = Get-Date -Format 'yyyyMMddHHmmss'
    $destination = Join-Path $BackupRoot "backup-$stamp"
    Write-Plan "copy contents of $ReleasePath to $destination"
    if ($DryRun) { return }
    Copy-ReleaseContent -Source $ReleasePath -Destination $destination
    Write-Output "BACKUP_CREATED $destination"
}

function Restore-ReleaseBackup {
    if ([string]::IsNullOrWhiteSpace($BackupPath)) { throw 'ROLLBACK_REQUIRES_EXPLICIT_BACKUP_PATH' }
    $source = (Resolve-Path -LiteralPath $BackupPath).Path
    $resolvedRoot = (Resolve-Path -LiteralPath $BackupRoot).Path
    if (-not $source.StartsWith($resolvedRoot + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw 'ROLLBACK_BACKUP_MUST_BE_INSIDE_RELEASE_BACKUPS'
    }
    if (-not (Test-Path -LiteralPath (Join-Path $source 'update.json'))) { throw 'ROLLBACK_BACKUP_MISSING_UPDATE_MANIFEST' }
    $stamp = Get-Date -Format 'yyyyMMddHHmmss'
    $stage = Join-Path $Root "release-restore-$stamp"
    $previous = Join-Path $Root "release-previous-$stamp"
    Write-Plan "stage $source at $stage, then move $ReleasePath to $previous and activate staged release"
    if ($DryRun) { return }
    if (-not $ConfirmRollback) { throw 'ROLLBACK_REQUIRES_CONFIRMROLLBACK' }
    Copy-ReleaseContent -Source $source -Destination $stage
    Move-Item -LiteralPath $ReleasePath -Destination $previous
    Move-Item -LiteralPath $stage -Destination $ReleasePath
    Write-Output "ROLLBACK_COMPLETE previous=$previous active=$ReleasePath"
}

function Copy-ReleaseContent([string]$Source, [string]$Destination) {
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $Destination $_.Name) -Recurse -Force
    }
}

switch ($Action) {
    'enable' {
        Set-Maintenance $true
        Write-Output $(if ($DryRun) { 'DRY_RUN: MAINTENANCE_ENABLED' } else { 'MAINTENANCE_ENABLED' })
    }
    'disable' {
        Set-Maintenance $false
        Write-Output $(if ($DryRun) { 'DRY_RUN: MAINTENANCE_DISABLED' } else { 'MAINTENANCE_DISABLED' })
    }
    'status' {
        $enabled = $false
        $manifest = Join-Path $ReleasePath 'update.json'
        if (Test-Path -LiteralPath $manifest) { $enabled = [bool]((Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json).maintenance.enabled) }
        Write-Output $(if ($enabled) { 'MAINTENANCE_ENABLED' } else { 'MAINTENANCE_DISABLED' })
    }
    'backup' { New-ReleaseBackup }
    'rollback' { Restore-ReleaseBackup }
}
