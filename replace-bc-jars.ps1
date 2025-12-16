# Script để thay Bouncy Castle jars vào EJBCA
# Sử dụng: .\replace-bc-jars.ps1 -EjbcaLibPath "C:\ejbca\lib"
# Hoặc: .\replace-bc-jars.ps1 (sẽ tự động tìm)

param(
    [string]$EjbcaLibPath = ""
)

function Get-LatestJar {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Pattern
    )

    $jar = Get-ChildItem $Pattern -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $jar) {
        throw "Jar not found for pattern: $Pattern"
    }

    return $jar
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Replacing Bouncy Castle Jars in EJBCA" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Nếu không có đường dẫn, thử tìm tự động
if ([string]::IsNullOrEmpty($EjbcaLibPath)) {
    Write-Host "EJBCA lib path not provided, searching..." -ForegroundColor Yellow
    
    $possiblePaths = @(
        "C:\ejbca\lib",
        "C:\Program Files\EJBCA\lib",
        "C:\Program Files (x86)\EJBCA\lib",
        "D:\ejbca\lib",
        "E:\ejbca\lib"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $EjbcaLibPath = $path
            Write-Host "Found EJBCA lib at: $EjbcaLibPath" -ForegroundColor Green
            break
        }
    }
    
    # Nếu vẫn không tìm thấy, báo lỗi
    if ([string]::IsNullOrEmpty($EjbcaLibPath)) {
        Write-Host "ERROR: EJBCA lib directory not found automatically." -ForegroundColor Red
        Write-Host "Please provide the path using: .\replace-bc-jars.ps1 -EjbcaLibPath 'C:\ejbca\lib'" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Or create a test directory:" -ForegroundColor Yellow
        Write-Host "  mkdir test-ejbca-lib" -ForegroundColor Gray
        Write-Host "  .\replace-bc-jars.ps1 -EjbcaLibPath 'test-ejbca-lib'" -ForegroundColor Gray
        exit 1
    }
}

# Kiểm tra thư mục EJBCA tồn tại
if (-not (Test-Path $EjbcaLibPath)) {
    Write-Host "ERROR: EJBCA lib directory not found: $EjbcaLibPath" -ForegroundColor Red
    Write-Host "Please check the path and try again." -ForegroundColor Red
    exit 1
}

Write-Host "Using EJBCA lib path: $EjbcaLibPath" -ForegroundColor Cyan
Write-Host ""

# Tìm jars mới (lấy bản mới nhất vừa build)
try {
    $bcprovJar = Get-LatestJar "$PSScriptRoot\prov\build\libs\bcprov-*.jar"
    $bcpkixJar = Get-LatestJar "$PSScriptRoot\pkix\build\libs\bcpkix-*.jar"
} catch {
    Write-Host "ERROR: $_" -ForegroundColor Red
    Write-Host "Please build the project first: .\gradlew :prov:jar :pkix:jar" -ForegroundColor Yellow
    exit 1
}

Write-Host "Found jars:" -ForegroundColor Yellow
Write-Host "  bcprov:  $($bcprovJar.FullName)" -ForegroundColor Gray
Write-Host "  bcpkix:  $($bcpkixJar.FullName)" -ForegroundColor Gray
Write-Host ""

# Backup jars cũ
Write-Host "Step 1: Backing up old jars..." -ForegroundColor Yellow
$backupDir = "$EjbcaLibPath\backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

$backedUp = $false
Get-ChildItem "$EjbcaLibPath\bcprov-*.jar" -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item $_.FullName $backupDir
    Write-Host "  Backed up: $($_.Name)" -ForegroundColor Gray
    $backedUp = $true
}

Get-ChildItem "$EjbcaLibPath\bcpkix-*.jar" -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item $_.FullName $backupDir
    Write-Host "  Backed up: $($_.Name)" -ForegroundColor Gray
    $backedUp = $true
}

if ($backedUp) {
    Write-Host "  Backup location: $backupDir" -ForegroundColor Green
} else {
    Write-Host "  No old jars found to backup" -ForegroundColor Gray
}
Write-Host ""

# Copy jars mới
Write-Host "Step 2: Copying new jars..." -ForegroundColor Yellow
$targetBcprov = Join-Path $EjbcaLibPath $bcprovJar.Name
$targetBcpkix = Join-Path $EjbcaLibPath $bcpkixJar.Name

Copy-Item $bcprovJar.FullName $targetBcprov -Force
Write-Host "  Copied: $($bcprovJar.Name)" -ForegroundColor Green

Copy-Item $bcpkixJar.FullName $targetBcpkix -Force
Write-Host "  Copied: $($bcpkixJar.Name)" -ForegroundColor Green
Write-Host ""

# Xóa jars cũ (nếu có version khác)
Write-Host "Step 3: Removing old jar versions..." -ForegroundColor Yellow
$removed = $false
Get-ChildItem "$EjbcaLibPath\bcprov-*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -ne $bcprovJar.Name } | ForEach-Object {
    Remove-Item $_.FullName -Force
    Write-Host "  Removed: $($_.Name)" -ForegroundColor Gray
    $removed = $true
}

Get-ChildItem "$EjbcaLibPath\bcpkix-*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -ne $bcpkixJar.Name } | ForEach-Object {
    Remove-Item $_.FullName -Force
    Write-Host "  Removed: $($_.Name)" -ForegroundColor Gray
    $removed = $true
}

if (-not $removed) {
    Write-Host "  No old jar versions to remove" -ForegroundColor Gray
}
Write-Host ""

# Verify jars đã được copy
Write-Host "Step 4: Verifying jars..." -ForegroundColor Yellow
if (Test-Path $targetBcprov) {
    $size = (Get-Item $targetBcprov).Length / 1MB
    Write-Host "  $($bcprovJar.Name): OK ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "  $($bcprovJar.Name): NOT FOUND" -ForegroundColor Red
}

if (Test-Path $targetBcpkix) {
    $size = (Get-Item $targetBcpkix).Length / 1MB
    Write-Host "  $($bcpkixJar.Name): OK ($([math]::Round($size, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "  $($bcpkixJar.Name): NOT FOUND" -ForegroundColor Red
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "SUCCESS! Jars replaced successfully." -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart EJBCA application server" -ForegroundColor White
Write-Host "2. Verify curve is available in EJBCA Admin UI" -ForegroundColor White
Write-Host "3. Test certificate generation with custom curve" -ForegroundColor White
Write-Host ""

