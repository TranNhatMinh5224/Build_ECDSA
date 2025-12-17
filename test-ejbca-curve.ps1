# Script để test custom curve trong EJBCA
# Sử dụng: .\test-ejbca-curve.ps1 -EjbcaLibPath "C:\ejbca\lib"

param(
    [Parameter(Mandatory=$true)]
    [string]$EjbcaLibPath
)

function Get-LatestLibJar {
    param(
        [Parameter(Mandatory = $true)]
        [string]$LibPath,
        [Parameter(Mandatory = $true)]
        [string]$Pattern
    )

    $jar = Get-ChildItem (Join-Path $LibPath $Pattern) -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $jar) {
        throw "Jar not found in $LibPath with pattern $Pattern"
    }

    return $jar
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Testing Custom Curve in EJBCA" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra thư mục EJBCA
if (-not (Test-Path $EjbcaLibPath)) {
    Write-Host "ERROR: EJBCA lib directory not found: $EjbcaLibPath" -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Replacing Bouncy Castle jars..." -ForegroundColor Yellow
try {
    & ".\replace-bc-jars.ps1" -EjbcaLibPath $EjbcaLibPath
    if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) {
        Write-Host "WARNING: replace-bc-jars.ps1 returned exit code: $LASTEXITCODE" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR: Failed to replace jars: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Tạo script Java để test curve
Write-Host "Step 2: Creating Java test script..." -ForegroundColor Yellow
$testJava = @"
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.bouncycastle.asn1.custom.CustomCurveObjectIdentifiers.myCustomCurve256;

public class TestEJBCACurve {
    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("Testing Custom Curve in EJBCA Context");
            System.out.println("========================================");
            System.out.println();
            
            // Add Bouncy Castle provider
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("Bouncy Castle provider added");
            System.out.println();
            
            // Test 1: ECNamedCurveTable.getByName
            System.out.println("Test 1: ECNamedCurveTable.getByName(\"MyCustomCurve-256\")");
            X9ECParameters params1 = ECNamedCurveTable.getByName("MyCustomCurve-256");
            if (params1 != null) {
                System.out.println("  PASS: Curve found");
                System.out.println("  Order (n): " + params1.getN().toString(16));
                System.out.println("  Cofactor (h): " + params1.getH());
            } else {
                System.out.println("  FAIL: Curve not found");
                System.exit(1);
            }
            System.out.println();
            
            // Test 2: ECNamedCurveTable.getOID
            System.out.println("Test 2: ECNamedCurveTable.getOID(\"MyCustomCurve-256\")");
            ASN1ObjectIdentifier oid1 = ECNamedCurveTable.getOID("MyCustomCurve-256");
            if (oid1 != null && oid1.equals(myCustomCurve256)) {
                System.out.println("  PASS: OID = " + oid1.getId());
            } else {
                System.out.println("  FAIL: OID not found or incorrect");
                if (oid1 != null) {
                    System.out.println("  Found OID: " + oid1.getId());
                }
                System.exit(1);
            }
            System.out.println();
            
            // Test 3: CustomNamedCurves.getByOID
            System.out.println("Test 3: CustomNamedCurves.getByOID(myCustomCurve256)");
            X9ECParameters params2 = CustomNamedCurves.getByOID(myCustomCurve256);
            if (params2 != null) {
                System.out.println("  PASS: Curve found by OID");
            } else {
                System.out.println("  FAIL: Curve not found by OID");
                System.exit(1);
            }
            System.out.println();
            
            // Test 4: Generate key pair
            System.out.println("Test 4: Generate key pair with ECGenParameterSpec");
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
                keyGen.initialize(new ECGenParameterSpec("MyCustomCurve-256"));
                KeyPair keyPair = keyGen.generateKeyPair();
                System.out.println("  PASS: Key pair generated successfully");
                System.out.println("  Public key algorithm: " + keyPair.getPublic().getAlgorithm());
                System.out.println("  Public key format: " + keyPair.getPublic().getFormat());
            } catch (Exception e) {
                System.out.println("  FAIL: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println();
            
            System.out.println("========================================");
            System.out.println("ALL TESTS PASSED!");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Next steps:");
            System.out.println("1. Restart EJBCA application server");
            System.out.println("2. Login to EJBCA Admin UI");
            System.out.println("3. Go to Certificate Profiles or Key Generation");
            System.out.println("4. Select 'MyCustomCurve-256' from curve list");
            System.out.println("5. Generate certificate and verify OID: 1.3.6.1.4.1.99999.1");
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
"@

$testJavaFile = "TestEJBCACurve.java"
# Use UTF8NoBOM encoding to avoid BOM issues
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Resolve-Path .).Path + "\" + $testJavaFile, $testJava, $utf8NoBom)
Write-Host "  Created: $testJavaFile" -ForegroundColor Green
Write-Host ""

# Compile và chạy test
Write-Host "Step 3: Compiling test..." -ForegroundColor Yellow
$bcprovJar = Get-LatestLibJar -LibPath $EjbcaLibPath -Pattern "bcprov-*.jar"
$bcpkixJar = Get-LatestLibJar -LibPath $EjbcaLibPath -Pattern "bcpkix-*.jar"

Write-Host "  Using bcprov: $($bcprovJar.Name)" -ForegroundColor Gray
Write-Host "  Using bcpkix: $($bcpkixJar.Name)" -ForegroundColor Gray

$classpath = "$($bcprovJar.FullName);$($bcpkixJar.FullName)"
javac -cp $classpath $testJavaFile 2>&1 | ForEach-Object {
    if ($_ -match "error") {
        Write-Host $_ -ForegroundColor Red
    } else {
        Write-Host $_ -ForegroundColor Gray
    }
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "  Compilation successful" -ForegroundColor Green
    Write-Host ""
    
    Write-Host "Step 4: Running test..." -ForegroundColor Yellow
    java -cp ".;$classpath" TestEJBCACurve
} else {
    Write-Host "  Compilation failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Test completed!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

