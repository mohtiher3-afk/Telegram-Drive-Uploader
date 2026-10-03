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

# Gradle caches previous test outcomes under <module>/build/test-results and
# reads them back through SerializableTestResult before it runs anything. A
# corrupt or truncated file there aborts the task with
#   java.lang.IllegalArgumentException: Illegal Capacity: <negative>
# before a single test executes, which looks exactly like a real test failure
# and silently invalidates any "tests passed" claim made from stale results.
#
# Clear those cached results first so every run reports outcomes it actually
# produced. The cost is that :data and :feature re-run their tests even when
# Gradle would have marked them up-to-date, which is the point: up-to-date test
# results are not evidence.
if ($Mode -eq 'CLEAN' -or $Mode -eq 'FULL' -or $Mode -eq 'RELEASE' -or $Mode -eq 'QUICK') {
    $stale = @()
    foreach ($module in 'app', 'core', 'data', 'domain', 'feature') {
        $results = Join-Path $RootDir "$module\build\test-results"
        if (Test-Path $results) {
            Remove-Item -Recurse -Force $results -ErrorAction SilentlyContinue
            $stale += $module
        }
    }
    if ($stale.Count -gt 0) {
        Write-Host ("[clean] Cleared cached test results: " + ($stale -join ', ')) -ForegroundColor Cyan
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
