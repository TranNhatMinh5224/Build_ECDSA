# Script để fix lỗi "Unsupported class file major version 67"
# Lỗi này xảy ra khi Gradle dùng Java version quá mới (Java 23)
# Gradle 8.5 cần Java 8, 11, 17 hoặc 21

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "FIX JAVA VERSION FOR GRADLE" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra Java hiện tại
Write-Host "Java version hiện tại:" -ForegroundColor Yellow
java -version
Write-Host ""

$currentJavaHome = $env:JAVA_HOME
Write-Host "JAVA_HOME hiện tại: $currentJavaHome" -ForegroundColor Yellow
Write-Host ""

# Tìm Java version phù hợp
$preferredVersions = @("BC_JDK21", "BC_JDK17", "BC_JDK11", "BC_JDK8")
$foundJava = $null
$foundVersion = $null

foreach ($var in $preferredVersions) {
    $value = [Environment]::GetEnvironmentVariable($var, "Process")
    if (-not $value) {
        $value = [Environment]::GetEnvironmentVariable($var, "User")
    }
    if (-not $value) {
        $value = [Environment]::GetEnvironmentVariable($var, "Machine")
    }
    
    if ($value -and (Test-Path $value)) {
        $foundJava = $value
        $foundVersion = $var
        Write-Host "✅ Tìm thấy: $var = $value" -ForegroundColor Green
        break
    } else {
        Write-Host "❌ $var = NOT SET hoặc không tồn tại" -ForegroundColor Red
    }
}

if (-not $foundJava) {
    Write-Host ""
    Write-Host "❌ KHÔNG TÌM THẤY JDK PHÙ HỢP!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vui lòng set các biến môi trường:" -ForegroundColor Yellow
    Write-Host "  - BC_JDK21 (khuyến nghị)" -ForegroundColor White
    Write-Host "  - BC_JDK17" -ForegroundColor White
    Write-Host "  - BC_JDK11" -ForegroundColor White
    Write-Host "  - BC_JDK8" -ForegroundColor White
    Write-Host ""
    Write-Host "Ví dụ:" -ForegroundColor Yellow
    Write-Host '  $env:BC_JDK21 = "C:\Program Files\Java\jdk-21"' -ForegroundColor White
    exit 1
}

# Verify Java version
$javaExe = Join-Path $foundJava "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Write-Host "❌ Không tìm thấy java.exe tại: $javaExe" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Đang kiểm tra Java version..." -ForegroundColor Yellow
$javaVersionOutput = & $javaExe -version 2>&1
Write-Host $javaVersionOutput

# Set JAVA_HOME tạm thời cho session này
$env:JAVA_HOME = $foundJava
$env:PATH = "$foundJava\bin;$env:PATH"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ ĐÃ SET JAVA_HOME = $foundJava" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "JAVA_HOME mới:" -ForegroundColor Yellow
Write-Host "  $env:JAVA_HOME" -ForegroundColor White
Write-Host ""
Write-Host "Java version mới:" -ForegroundColor Yellow
java -version
Write-Host ""
Write-Host "💡 LƯU Ý: Thay đổi này chỉ có hiệu lực trong session PowerShell hiện tại." -ForegroundColor Yellow
Write-Host "   Để set vĩnh viễn, chạy script này trước khi build:" -ForegroundColor Yellow
Write-Host "   . .\fix-java-version.ps1" -ForegroundColor Cyan
Write-Host "   .\build-with-log.ps1" -ForegroundColor Cyan
Write-Host ""








