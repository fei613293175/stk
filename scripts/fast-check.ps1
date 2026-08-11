$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Python = $env:STK_PYTHON
if (-not $Python) {
    $PythonCommand = Get-Command python -ErrorAction SilentlyContinue
    if ($PythonCommand) { $Python = $PythonCommand.Source }
}
if (-not $Python) { throw "Python 3 未安装，或未通过 STK_PYTHON 指定解释器。" }
& $Python (Join-Path $Root "scripts\validate-kit.py")
if ($LASTEXITCODE -ne 0) {
    throw "Python 3 不可用，或静态合同检查失败。可通过 STK_PYTHON 指定解释器。"
}
$Php = Get-Command php -ErrorAction SilentlyContinue
if ($Php) {
    Get-ChildItem (Join-Path $Root "discuz") -Recurse -Filter *.php | ForEach-Object {
        & $Php.Source -l $_.FullName | Out-Null
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    & $Php.Source (Join-Path $Root "discuz\tests\dev_auth_service_test.php")
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $Php.Source (Join-Path $Root "discuz\tests\project_contract_test.php")
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $Php.Source (Join-Path $Root "discuz\tests\v130_contract_test.php")
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} else {
    Write-Host "SKIP: php 未安装；GitHub Fast CI 会执行 PHP 检查。"
}
Write-Host "PASS: STK V1.3.0 fast static checks"
