# --- EDIT THE LINE BELOW ---
$env:GOOGLE_API_KEY = "PASTE_YOUR_KEY_HERE"
# ---------------------------

if ($env:GOOGLE_API_KEY -eq "PASTE_YOUR_KEY_HERE") {
    Write-Host "ERROR: You forgot to paste your key into this file!" -ForegroundColor Red
    return
}

Write-Host "API Key is set. Starting Payroll Agent..." -ForegroundColor Green
Set-Location payroll-automation
.\mvnw.cmd spring-boot:run
