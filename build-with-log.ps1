# Script build với logging chi tiết
# Usage: .\build-with-log.ps1 [task] [options]

param(
    [string]$Task = "build",
    [string]$LogDir = "build_logs",
    [switch]$Clean = $false
)

# Tạo thư mục log nếu chưa có
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir | Out-Null
}

# Tên file log với timestamp
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $LogDir "build_$timestamp.log"
$errorLogFile = Join-Path $LogDir "build_errors_$timestamp.log"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "BUILD WITH LOGGING" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Task: $Task" -ForegroundColor Yellow
Write-Host "Log file: $logFile" -ForegroundColor Yellow
Write-Host "Error log: $errorLogFile" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Function để log
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss.SSS"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    # Ghi vào console
    switch ($Level) {
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        "WARN"  { Write-Host $logMessage -ForegroundColor Yellow }
        "INFO"  { Write-Host $logMessage -ForegroundColor Green }
        default { Write-Host $logMessage }
    }
    
    # Ghi vào file
    Add-Content -Path $logFile -Value $logMessage
}

# Function để log error
function Write-Error-Log {
    param([string]$Message, [string]$ErrorDetails = "")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss.SSS"
    $logMessage = "[$timestamp] [ERROR] $Message"
    
    Write-Host $logMessage -ForegroundColor Red
    Add-Content -Path $logFile -Value $logMessage
    Add-Content -Path $errorLogFile -Value $logMessage
    
    if ($ErrorDetails) {
        Add-Content -Path $logFile -Value $ErrorDetails
        Add-Content -Path $errorLogFile -Value $ErrorDetails
    }
}

# Kiểm tra Gradle wrapper
if (-not (Test-Path "gradlew.bat")) {
    Write-Error-Log "Không tìm thấy gradlew.bat! Đảm bảo bạn đang ở thư mục gốc của project."
    exit 1
}

# Kiểm tra JDK environment variables
Write-Log "Kiểm tra JDK environment variables..."
$jdkVars = @("BC_JDK8", "BC_JDK11", "BC_JDK17", "BC_JDK21")
$missingVars = @()

foreach ($var in $jdkVars) {
    $value = [Environment]::GetEnvironmentVariable($var, "Process")
    if (-not $value) {
        $value = [Environment]::GetEnvironmentVariable($var, "User")
    }
    if (-not $value) {
        $value = [Environment]::GetEnvironmentVariable($var, "Machine")
    }
    
    if ($value) {
        Write-Log "  ✓ $var = $value"
    } else {
        Write-Log "  ✗ $var = NOT SET" "WARN"
        $missingVars += $var
    }
}

if ($missingVars.Count -gt 0) {
    Write-Error-Log "Thiếu các biến môi trường: $($missingVars -join ', ')"
    Write-Error-Log "Vui lòng set các biến này trước khi build!"
    exit 1
}

# Clean nếu cần
if ($Clean) {
    Write-Log "Cleaning project..."
    & .\gradlew.bat clean 2>&1 | Tee-Object -FilePath $logFile -Append
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Log "Clean failed với exit code: $LASTEXITCODE"
    }
}

# Build với logging chi tiết
Write-Log "Bắt đầu build task: $Task"
Write-Log "=========================================="

$buildStartTime = Get-Date

# Chạy Gradle với logging chi tiết
# --stacktrace: hiển thị stack trace đầy đủ
# --info: log chi tiết
# --warning-mode: hiển thị tất cả warnings
$gradleArgs = @(
    $Task,
    "--stacktrace",
    "--info",
    "--warning-mode", "all"
)

Write-Log "Gradle command: .\gradlew.bat $($gradleArgs -join ' ')"

# Redirect cả stdout và stderr
$process = Start-Process -FilePath ".\gradlew.bat" `
    -ArgumentList $gradleArgs `
    -NoNewWindow `
    -PassThru `
    -RedirectStandardOutput "$logFile.tmp" `
    -RedirectStandardError "$errorLogFile.tmp" `
    -Wait

$buildEndTime = Get-Date
$buildDuration = $buildEndTime - $buildStartTime

# Đọc output và ghi vào log
if (Test-Path "$logFile.tmp") {
    $output = Get-Content "$logFile.tmp" -Raw
    Add-Content -Path $logFile -Value "`n=== GRADLE OUTPUT ==="
    Add-Content -Path $logFile -Value $output
    Write-Host $output
    Remove-Item "$logFile.tmp"
}

# Đọc error và ghi vào error log
if (Test-Path "$errorLogFile.tmp") {
    $errors = Get-Content "$errorLogFile.tmp" -Raw
    if ($errors) {
        Add-Content -Path $errorLogFile -Value "`n=== GRADLE ERRORS ==="
        Add-Content -Path $errorLogFile -Value $errors
        Add-Content -Path $logFile -Value "`n=== GRADLE ERRORS ==="
        Add-Content -Path $logFile -Value $errors
        Write-Host $errors -ForegroundColor Red
    }
    Remove-Item "$errorLogFile.tmp"
}

# Kết quả
Write-Log "=========================================="
Write-Log "Build duration: $($buildDuration.TotalSeconds) seconds"

if ($process.ExitCode -eq 0) {
    Write-Log "✅ BUILD THÀNH CÔNG!" "INFO"
    Write-Host ""
    Write-Host "✅ BUILD THÀNH CÔNG!" -ForegroundColor Green
    Write-Host "Log file: $logFile" -ForegroundColor Cyan
} else {
    Write-Error-Log "❌ BUILD THẤT BẠI với exit code: $($process.ExitCode)"
    Write-Host ""
    Write-Host "❌ BUILD THẤT BẠI!" -ForegroundColor Red
    Write-Host "Exit code: $($process.ExitCode)" -ForegroundColor Red
    Write-Host "Xem log file để biết chi tiết:" -ForegroundColor Yellow
    Write-Host "  - Full log: $logFile" -ForegroundColor Yellow
    Write-Host "  - Error log: $errorLogFile" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Cac loi pho bien:" -ForegroundColor Cyan
    Write-Host '  1. Thieu JDK environment variables (BC_JDK8, BC_JDK11, BC_JDK17, BC_JDK21)' -ForegroundColor White
    Write-Host '  2. Loi compile Java (xem trong log file)' -ForegroundColor White
    Write-Host '  3. Loi dependency (xem trong log file)' -ForegroundColor White
    exit $process.ExitCode
}

exit 0

