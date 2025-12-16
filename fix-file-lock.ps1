# Script để fix file lock issues trên Windows
# Usage: .\fix-file-lock.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "FIX FILE LOCK ISSUES" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Bước 1: Stop tất cả Gradle daemon
Write-Host "[1/4] Đang dừng Gradle daemon..." -ForegroundColor Yellow
try {
    & .\gradlew --stop 2>&1 | Out-Null
    Write-Host "  ✅ Đã dừng Gradle daemon" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Không thể dừng daemon (có thể đã dừng)" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Bước 2: Kill các Java process có thể đang giữ file
Write-Host "[2/4] Đang kiểm tra Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.Path -like "*jdk*" -or $_.Path -like "*java*" }
if ($javaProcesses) {
    Write-Host "  Tìm thấy $($javaProcesses.Count) Java process(es)" -ForegroundColor Yellow
    foreach ($proc in $javaProcesses) {
        try {
            $proc.Kill()
            Write-Host "  ✅ Đã kill process: $($proc.Id)" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠️  Không thể kill process: $($proc.Id)" -ForegroundColor Yellow
        }
    }
    Start-Sleep -Seconds 2
} else {
    Write-Host "  ✅ Không có Java process nào đang chạy" -ForegroundColor Green
}

# Bước 3: Xóa tất cả thư mục build
Write-Host "[3/4] Đang xóa tất cả thư mục build..." -ForegroundColor Yellow
$buildDirs = @(
    "build",
    "core\build",
    "prov\build",
    "util\build",
    "pkix\build",
    "tls\build",
    "pg\build",
    "mail\build",
    "jmail\build",
    "mls\build",
    "test\build"
)

$deletedCount = 0
foreach ($dir in $buildDirs) {
    if (Test-Path $dir) {
        try {
            # Thử xóa với retry
            $maxRetries = 3
            $retryCount = 0
            $deleted = $false
            
            while ($retryCount -lt $maxRetries -and -not $deleted) {
                try {
                    Remove-Item -Path $dir -Recurse -Force -ErrorAction Stop
                    $deleted = $true
                    $deletedCount++
                    Write-Host "  ✅ Đã xóa: $dir" -ForegroundColor Green
                } catch {
                    $retryCount++
                    if ($retryCount -lt $maxRetries) {
                        $msg = "  Warning: Retry $retryCount of $maxRetries - $dir"
                        Write-Host $msg -ForegroundColor Yellow
                        Start-Sleep -Seconds 1
                    } else {
                        Write-Host "  ❌ Không thể xóa: $dir" -ForegroundColor Red
                        Write-Host "     Lỗi: $($_.Exception.Message)" -ForegroundColor Red
                    }
                }
            }
        } catch {
            Write-Host "  ❌ Lỗi khi xóa $dir : $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# Bước 4: Xóa Gradle cache nếu cần
Write-Host "[4/4] Đang kiểm tra Gradle cache..." -ForegroundColor Yellow
$gradleCache = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $gradleCache) {
    Write-Host "  Info: Gradle cache at: $gradleCache" -ForegroundColor Cyan
    Write-Host "  Tip: If errors persist, you can manually delete the cache" -ForegroundColor Yellow
} else {
    Write-Host "  ✅ Không tìm thấy Gradle cache" -ForegroundColor Green
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ HOÀN THÀNH!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Đã xóa $deletedCount thư mục build" -ForegroundColor Green
Write-Host ""
Write-Host "Bây giờ bạn có thể chạy:" -ForegroundColor Yellow
Write-Host "  .\gradlew clean build" -ForegroundColor Cyan
Write-Host ""

