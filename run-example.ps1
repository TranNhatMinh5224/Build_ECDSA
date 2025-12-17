# Script để chạy CustomCurveExample với encoding UTF-8
# Chạy: .\run-example.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Running CustomCurveExample" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set encoding UTF-8 cho console
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# Chạy Gradle task
cd $PSScriptRoot
.\gradlew :test:runExample --console=plain

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Done!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan





