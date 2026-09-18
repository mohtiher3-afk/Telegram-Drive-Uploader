param()
$envPath = Join-Path $PSScriptRoot ".env"
$apiId = Read-Host "TELEGRAM_API_ID (رقم فقط)"
$apiHash = Read-Host "TELEGRAM_API_HASH"
$lines = @(
  "TELEGRAM_API_ID=$apiId",
  "TELEGRAM_API_HASH=$apiHash",
  "SENTRY_DSN="
)
Set-Content -Path $envPath -Value $lines -Encoding ascii -NoNewline -Force