<#
.SYNOPSIS
  Windows PowerShell entry point for the same verification the CI runs.
.EXAMPLE
  .\scripts\verify-project.ps1 -Mode QUICK
#>
param(
    [ValidateSet('QUICK', 'FULL', 'RELEASE', 'CLEAN')]
    [string]$Mode = 'FULL'
)

$ErrorActionPreference = 'Continue'

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

function Invoke-Step {
    param(
        [string]$Key,
        [string]$Title,
        [string[]]$Command
    )
    Write-Host ''
    Write-Host "[$Key] $Title" -ForegroundColor Cyan
    & $Command[0] @($Command[1..($Command.Count - 1)])
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[$Key] FAIL (exit $LASTEXITCODE)" -ForegroundColor Red
        $script:Failed = $true
    } else {
        Write-Host "[$Key] PASS" -ForegroundColor Green
    }
}

if ($Mode -eq 'CLEAN') {
    Write-Host '[clean] Clearing project build outputs' -ForegroundColor Cyan
    & (Join-Path $RootDir 'gradlew.bat') clean
}

$Failed = $false

$g = Join-Path $RootDir 'gradlew.bat'

Invoke-Step 'compile' 'Compile every module (debug)' @($g, '--no-daemon', '--max-workers=2',
    'compileDebugKotlin', 'compileDebugUnitTestKotlin')

Invoke-Step 'tests' 'Run every module unit tests' @($g, '--no-daemon', '--max-workers=2',
    ':app:testDebugUnitTest',
    ':core:testDebugUnitTest',
    ':domain:test',
    ':data:testDebugUnitTest',
    ':feature:testDebugUnitTest')

Invoke-Step 'lint' 'Lint every module (warnings are errors)' @($g, '--no-daemon', '--max-workers=2',
    'lintDebug')

Invoke-Step 'build' 'Assemble debug APK' @($g, '--no-daemon', '--max-workers=2', ':app:assembleDebug')

if ($Mode -eq 'RELEASE') {
    Invoke-Step 'release' 'Assemble release APK (R8 + lintVital)' @($g, '--no-daemon', '--max-workers=2',
        ':app:assembleRelease')
}

Write-Host ''
if ($Failed) {
    Write-Host 'VERIFICATION FAILED' -ForegroundColor Red
    exit 1
}
Write-Host 'VERIFICATION PASSED' -ForegroundColor Green
exit 0
