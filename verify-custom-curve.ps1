# Script để verify custom curve có trong Bouncy Castle jar
# Sử dụng: .\verify-custom-curve.ps1 -JarPath "path\to\bcprov-jdk18on-1.80.jar"
# Nếu không truyền JarPath, script sẽ tự tìm jar mới nhất trong build output.

param(
    [string]$JarPath = ""
)

function Get-LatestJar {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Patterns
    )

    $candidates = @()
    foreach ($pattern in $Patterns) {
        $candidates += Get-ChildItem $pattern -ErrorAction SilentlyContinue
    }

    $jar = $candidates | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    return $jar
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Verifying Custom Curve in Bouncy Castle Jar" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ([string]::IsNullOrEmpty($JarPath)) {
    $Jar = Get-LatestJar -Patterns @(
        "$PSScriptRoot\prov\build\libs\bcprov-*.jar",
        "$PSScriptRoot\test-ejbca-lib\bcprov-*.jar"
    )
    if (-not $Jar) {
        Write-Host "ERROR: Jar file not found. Please build first or provide -JarPath" -ForegroundColor Red
        exit 1
    }
} else {
    if (-not (Test-Path $JarPath)) {
        Write-Host "ERROR: Jar file not found: $JarPath" -ForegroundColor Red
        exit 1
    }
    $Jar = Get-Item $JarPath
}

$JarPath = $Jar.FullName

Write-Host "Checking jar: $JarPath" -ForegroundColor Yellow
Write-Host ""

# Kiểm tra các class quan trọng có trong jar
$requiredClasses = @(
    "org/bouncycastle/crypto/ec/CustomNamedCurves.class",
    "org/bouncycastle/asn1/custom/CustomCurveObjectIdentifiers.class"
)

Write-Host "Step 1: Checking required classes..." -ForegroundColor Yellow
$jarContent = jar -tf $JarPath 2>&1

$allFound = $true
foreach ($class in $requiredClasses) {
    if ($jarContent -match [regex]::Escape($class)) {
        Write-Host "  Found: $class" -ForegroundColor Green
    } else {
        Write-Host "  Missing: $class" -ForegroundColor Red
        $allFound = $false
    }
}
Write-Host ""

# Kiểm tra curve JSON file có tồn tại
Write-Host "Step 2: Checking curve parameters file..." -ForegroundColor Yellow
$curveFile = "curves\MyCustomCurve-256.json"
if (Test-Path $curveFile) {
    Write-Host "  Found: $curveFile" -ForegroundColor Green
    
    # Đọc và hiển thị thông tin curve
    $curveData = Get-Content $curveFile -Raw | ConvertFrom-Json
    Write-Host "  Curve name: $($curveData.name)" -ForegroundColor Cyan
    Write-Host "  p (hex, first 32 chars): $($curveData.p.Substring(0, [Math]::Min(32, $curveData.p.Length)))..." -ForegroundColor Gray
    Write-Host "  n (hex, first 32 chars): $($curveData.n.Substring(0, [Math]::Min(32, $curveData.n.Length)))..." -ForegroundColor Gray
    Write-Host "  h: $($curveData.h)" -ForegroundColor Gray
} else {
    Write-Host "  Not found: $curveFile" -ForegroundColor Yellow
    Write-Host "  (This is OK if curve is hardcoded in CustomNamedCurves)" -ForegroundColor Gray
}
Write-Host ""

# Test Java class có thể load được không
Write-Host "Step 3: Testing Java class loading..." -ForegroundColor Yellow
$testScript = @"
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import static org.bouncycastle.asn1.custom.CustomCurveObjectIdentifiers.myCustomCurve256;

public class TestCurve {
    public static void main(String[] args) {
        try {
            // Test 1: ECNamedCurveTable
            X9ECParameters params1 = ECNamedCurveTable.getByName("MyCustomCurve-256");
            if (params1 != null) {
                System.out.println("PASS: ECNamedCurveTable.getByName");
            } else {
                System.out.println("FAIL: ECNamedCurveTable.getByName");
            }
            
            // Test 2: CustomNamedCurves
            X9ECParameters params2 = CustomNamedCurves.getByOID(myCustomCurve256);
            if (params2 != null) {
                System.out.println("PASS: CustomNamedCurves.getByOID");
            } else {
                System.out.println("FAIL: CustomNamedCurves.getByOID");
            }
            
            // Test 3: OID
            System.out.println("OID: " + myCustomCurve256.getId());
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
"@

$testFile = "TestCurve.java"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($testFile, $testScript, $utf8NoBom)

Write-Host "  Created test file: $testFile" -ForegroundColor Gray
Write-Host "  (To run: javac -cp `"$JarPath`" $testFile && java -cp `".;$JarPath`" TestCurve)" -ForegroundColor Gray
Write-Host ""

if ($allFound) {
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "SUCCESS! Custom curve classes found in jar." -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Cyan
} else {
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "WARNING! Some classes are missing." -ForegroundColor Yellow
    Write-Host "==========================================" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "To use with EJBCA:" -ForegroundColor Yellow
Write-Host "1. Copy jars to EJBCA lib directory" -ForegroundColor White
Write-Host "2. Restart EJBCA application server" -ForegroundColor White
Write-Host "3. Verify curve in EJBCA Admin UI" -ForegroundColor White

