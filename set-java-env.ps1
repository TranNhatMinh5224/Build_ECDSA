# Script để set Java environment variables
# Chạy script này mỗi khi mở PowerShell mới: . .\set-java-env.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "SET JAVA ENVIRONMENT" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME về JDK 17 (hoặc JDK 8 nếu muốn)
$env:JAVA_HOME = "D:\jdk17"
$env:PATH = "D:\jdk17\bin;$env:PATH"

# Set BC_JDK* (bắt buộc cho project này)
$env:BC_JDK8 = "D:\jdk1.8.0_202"
$env:BC_JDK11 = "D:\jdk11"
$env:BC_JDK17 = "D:\jdk17"
$env:BC_JDK21 = "D:\jdk21"

Write-Host "✅ Đã set JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "✅ Đã set BC_JDK8 = $env:BC_JDK8" -ForegroundColor Green
Write-Host "✅ Đã set BC_JDK11 = $env:BC_JDK11" -ForegroundColor Green
Write-Host "✅ Đã set BC_JDK17 = $env:BC_JDK17" -ForegroundColor Green
Write-Host "✅ Đã set BC_JDK21 = $env:BC_JDK21" -ForegroundColor Green
Write-Host ""

Write-Host "Java version:" -ForegroundColor Yellow
java -version
Write-Host ""

Write-Host "✅ Sẵn sàng build!" -ForegroundColor Green
Write-Host ""
Write-Host "Để build, chạy:" -ForegroundColor Yellow
Write-Host "  .\build-with-log.ps1" -ForegroundColor Cyan
Write-Host ""








