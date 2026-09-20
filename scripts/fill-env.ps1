#Requires -Version 5.1
<#
.SYNOPSIS
    Creates a local .env file with the Telegram API credentials required for a build.

.DESCRIPTION
    The .env file is git-ignored and must never be committed. Obtain an API ID and API
    hash from https://my.telegram.org and run this script from the repository root.

.EXAMPLE
    pwsh ./scripts/fill-env.ps1
#>
[CmdletBinding()]
param(
    [string]$EnvPath
)

$ErrorActionPreference = 'Stop'

if (-not $EnvPath) {
    # Default to the repository root regardless of the current directory.
    $repoRoot = Split-Path -Parent $PSScriptRoot
    $EnvPath = Join-Path $repoRoot '.env'
}

$apiId = Read-Host 'TELEGRAM_API_ID (digits only)'
if ($apiId -notmatch '^\d+$') {
    Write-Error 'TELEGRAM_API_ID must contain digits only.'
}

$apiHash = Read-Host 'TELEGRAM_API_HASH'
if ([string]::IsNullOrWhiteSpace($apiHash)) {
    Write-Error 'TELEGRAM_API_HASH must not be empty.'
}

$lines = @(
    "TELEGRAM_API_ID=$apiId",
    "TELEGRAM_API_HASH=$apiHash",
    'SENTRY_DSN='
)

Set-Content -Path $EnvPath -Value $lines -Encoding ascii -Force
Write-Host "Wrote $EnvPath (git-ignored). Never commit this file."
