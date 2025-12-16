# QUICK FIX: Set Java 8 và clear cache
# Chạy script này trước khi build

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "QUICK FIX JAVA VERSION" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Tìm Java 8
$java8Paths = @(
    "C:\Program Files\Java\jdk1.8.0_441",
    "C:\Program Files\Java\jdk1.8.0_xxx",
    "C:\Program Files (x86)\Java\jdk1.8.0_441",
    "C:\Program Files (x86)\Java\jdk1.8.0_xxx"
)

$foundJdk = $null
foreach ($path in $java8Paths) {
    if (Test-Path $path) {
        $javacPath = Join-Path $path "bin\javac.exe"
        if (Test-Path $javacPath) {
            $foundJdk = $path
            Write-Host "✅ Tìm thấy JDK 8 tại: $foundJdk" -ForegroundColor Green
            break
        }
    }
}

if (-not $foundJdk) {
    Write-Host "❌ KHÔNG TÌM THẤY JDK 8!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Bạn cần:" -ForegroundColor Yellow
    Write-Host "1. Cài đặt JDK 8 (không phải JRE)" -ForegroundColor White
    Write-Host "   Download: https://adoptium.net/temurin/releases/?version=8" -ForegroundColor White
    Write-Host "   Hoặc: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Sau khi cài, chạy lại script này" -ForegroundColor White
    Write-Host ""
    Write-Host "Hoặc nếu bạn có JDK 11/17/21, hãy set thủ công:" -ForegroundColor Yellow
    Write-Host '   $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"' -ForegroundColor Cyan
    Write-Host '   $env:BC_JDK21 = "C:\Program Files\Java\jdk-21"' -ForegroundColor Cyan
    exit 1
}

# Set JAVA_HOME
$env:JAVA_HOME = $foundJdk
$env:PATH = "$foundJdk\bin;$env:PATH"

# Set BC_JDK* (tạm thời dùng JDK 8 cho tất cả)
$env:BC_JDK8 = $foundJdk
$env:BC_JDK11 = $foundJdk
$env:BC_JDK17 = $foundJdk
$env:BC_JDK21 = $foundJdk

Write-Host ""
Write-Host "✅ Đã set JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
Write-Host "✅ Đã set BC_JDK* = $foundJdk" -ForegroundColor Green
Write-Host ""

# Verify
Write-Host "Java version:" -ForegroundColor Yellow
java -version
Write-Host ""

# Clear Gradle cache
Write-Host "Đang xóa Gradle cache..." -ForegroundColor Yellow
$gradleCache = "$env:USERPROFILE\.gradle\caches\8.5\scripts"
if (Test-Path $gradleCache) {
    Remove-Item $gradleCache -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✅ Đã xóa Gradle cache" -ForegroundColor Green
} else {
    Write-Host "⚠️  Gradle cache không tồn tại" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ HOÀN TẤT!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Bây giờ bạn có thể build:" -ForegroundColor Yellow
Write-Host "  .\build-with-log.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Lưu ý: Thay đổi này chỉ có hiệu lực trong session hiện tại." -ForegroundColor Yellow
Write-Host "   Để set vĩnh viễn, thêm vào Environment Variables của Windows." -ForegroundColor Yellow
Write-Host ""








