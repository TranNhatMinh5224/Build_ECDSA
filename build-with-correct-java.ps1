# Script build với Java version đúng
# Tự động fix Java version trước khi build

# Load fix-java-version script
. .\fix-java-version.ps1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Không thể set Java version đúng. Dừng build." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Bắt đầu build với Java version đúng..." -ForegroundColor Green
Write-Host ""

# Chạy build script
& .\build-with-log.ps1 @args








