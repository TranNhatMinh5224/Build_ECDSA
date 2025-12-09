# 🚀 HƯỚNG DẪN CHẠY DỰ ÁN BOUNCY CASTLE (NCKK-ECDSA)

## ⚠️ LƯU Ý QUAN TRỌNG

**Dự án này là THƯ VIỆN (Library), KHÔNG phải ứng dụng chạy độc lập!**

Bạn không thể "chạy" nó như Spring Boot (`java -jar app.jar`). 

Thay vào đó, bạn có 3 cách sử dụng:

---

## 📋 MỤC LỤC

1. [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
2. [Cách 1: Build thư viện](#cách-1-build-thư-viện-để-sử-dụng)
3. [Cách 2: Chạy Tests](#cách-2-chạy-tests-để-xem-hoạt-động)
4. [Cách 3: Tạo demo project](#cách-3-tạo-demo-project-để-test)
5. [Troubleshooting](#troubleshooting)

---

## 🔧 YÊU CẦU HỆ THỐNG

### 1. Cài đặt Java (Bắt buộc)

Bạn cần cài **4 phiên bản JDK**:

```powershell
# Kiểm tra Java đã cài chưa
java -version
```

**Download JDK:**
- [JDK 8](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
- [JDK 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

**Hoặc dùng OpenJDK:**
- [Adoptium/Temurin](https://adoptium.net/)
- [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)

### 2. Thiết lập biến môi trường

#### Windows PowerShell:
```powershell
# Mở PowerShell với quyền Admin và chạy:

# Set environment variables (tạm thời cho session hiện tại)
$env:BC_JDK8="C:\Program Files\Java\jdk1.8.0_xxx"
$env:BC_JDK11="C:\Program Files\Java\jdk-11.x.x"
$env:BC_JDK17="C:\Program Files\Java\jdk-17.x.x"
$env:BC_JDK21="C:\Program Files\Java\jdk-21.x.x"

# Hoặc set vĩnh viễn (System Environment Variables)
[System.Environment]::SetEnvironmentVariable('BC_JDK8', 'C:\Program Files\Java\jdk1.8.0_xxx', 'User')
[System.Environment]::SetEnvironmentVariable('BC_JDK11', 'C:\Program Files\Java\jdk-11.x.x', 'User')
[System.Environment]::SetEnvironmentVariable('BC_JDK17', 'C:\Program Files\Java\jdk-17.x.x', 'User')
[System.Environment]::SetEnvironmentVariable('BC_JDK21', 'C:\Program Files\Java\jdk-21.x.x', 'User')

# Kiểm tra
echo $env:BC_JDK8
echo $env:BC_JDK11
echo $env:BC_JDK17
echo $env:BC_JDK21
```

#### Hoặc set qua GUI:
1. `Windows Key` → Gõ "Environment Variables"
2. Chọn "Edit the system environment variables"
3. Click "Environment Variables..."
4. Thêm các biến: `BC_JDK8`, `BC_JDK11`, `BC_JDK17`, `BC_JDK21`

### 3. Set JAVA_HOME
```powershell
# JAVA_HOME phải trỏ đến JDK 17 hoặc 21
$env:JAVA_HOME="C:\Program Files\Java\jdk-17.x.x"

# Hoặc vĩnh viễn:
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-17.x.x', 'User')
```

---

## 🎯 CÁCH 1: BUILD THƯ VIỆN (ĐỂ SỬ DỤNG)

### Bước 1: Mở Terminal/PowerShell

```powershell
# Di chuyển vào thư mục dự án
cd C:\Users\Minh\Desktop\Nckk-ecdsa\NCKK-ECDSA
```

### Bước 2: Build toàn bộ project

```powershell
# Windows
.\gradlew clean build

# Nếu báo lỗi permission:
.\gradlew.bat clean build
```

**Output:**
```
BUILD SUCCESSFUL in 2m 15s
```

### Bước 3: Tìm file JAR đã build

Sau khi build thành công, các file JAR sẽ nằm ở:

```
NCKK-ECDSA/
├── core/build/libs/
│   └── bcprov-jdk18on-1.xx.jar          ← Core cryptography
├── prov/build/libs/
│   └── bcprov-jdk18on-1.xx.jar          ← JCA/JCE Provider
├── pkix/build/libs/
│   └── bcpkix-jdk18on-1.xx.jar          ← PKI/X.509
├── tls/build/libs/
│   └── bctls-jdk18on-1.xx.jar           ← TLS/SSL
├── pg/build/libs/
│   └── bcpg-jdk18on-1.xx.jar            ← OpenPGP
└── mail/build/libs/
    └── bcmail-jdk18on-1.xx.jar          ← S/MIME
```

### Bước 4: Sử dụng JAR trong project khác

**Ví dụ trong Gradle project:**
```gradle
dependencies {
    // Thêm file JAR vừa build
    implementation files('path/to/bcprov-jdk18on-1.xx.jar')
    implementation files('path/to/bcpkix-jdk18on-1.xx.jar')
}
```

**Hoặc copy vào `libs/` folder:**
```
YourProject/
├── libs/
│   ├── bcprov-jdk18on-1.xx.jar
│   └── bcpkix-jdk18on-1.xx.jar
└── build.gradle
```

---

## 🧪 CÁCH 2: CHẠY TESTS (ĐỂ XEM HOẠT ĐỘNG)

### Tests là cách tốt nhất để xem thư viện hoạt động như thế nào!

### Chạy all tests:
```powershell
# Chạy tất cả tests
.\gradlew test

# Chạy tests cụ thể cho một module
.\gradlew :core:test
.\gradlew :prov:test
.\gradlew :pkix:test
```

### Chạy tests trên nhiều Java versions:
```powershell
# Test trên Java 11
.\gradlew test11

# Test trên Java 17
.\gradlew test17

# Test trên Java 21
.\gradlew test21

# Chạy tất cả
.\gradlew clean build test11 test17 test21
```

### Xem kết quả tests:
```
# Test reports sẽ nằm ở:
test/build/reports/tests/test/index.html
prov/build/reports/tests/test/index.html
core/build/reports/tests/test/index.html

# Mở bằng browser để xem
```

### Chạy một test cụ thể:
```powershell
# Ví dụ: Chạy ECDSA test
.\gradlew :prov:test --tests "*ECDSATest*"

# Hoặc
.\gradlew :core:test --tests "*ECTest*"
```

---

## 💻 CÁCH 3: TẠO DEMO PROJECT ĐỂ TEST

### Option A: Tạo Java Console App đơn giản

#### 1. Tạo folder mới:
```powershell
mkdir C:\Users\Minh\Desktop\BC-Demo
cd C:\Users\Minh\Desktop\BC-Demo
```

#### 2. Tạo file `build.gradle`:
```gradle
plugins {
    id 'java'
    id 'application'
}

group = 'com.example'
version = '1.0.0'

sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    // Sử dụng Bouncy Castle từ Maven Central
    implementation 'org.bouncycastle:bcprov-jdk18on:1.77'
    implementation 'org.bouncycastle:bcpkix-jdk18on:1.77'
}

application {
    mainClass = 'com.example.Main'
}
```

#### 3. Tạo file `src/main/java/com/example/Main.java`:
```java
package com.example;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("=== DEMO BOUNCY CASTLE ECDSA ===\n");
        
        // 1. Đăng ký Bouncy Castle Provider
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("✓ Bouncy Castle Provider registered");
        
        // 2. Tạo EC Key Pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1"); // P-256
        keyGen.initialize(ecSpec, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        
        System.out.println("✓ EC Key Pair generated (secp256r1)");
        System.out.println("  Private Key: " + keyPair.getPrivate().getAlgorithm());
        System.out.println("  Public Key: " + keyPair.getPublic().getAlgorithm());
        
        // 3. Ký dữ liệu
        String message = "Hello Bouncy Castle ECDSA!";
        byte[] data = message.getBytes();
        
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(data);
        byte[] signature = signer.sign();
        
        System.out.println("\n✓ Message signed:");
        System.out.println("  Message: " + message);
        System.out.println("  Signature: " + Base64.getEncoder().encodeToString(signature));
        
        // 4. Verify chữ ký
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(data);
        boolean isValid = verifier.verify(signature);
        
        System.out.println("\n✓ Signature verification: " + (isValid ? "VALID ✓" : "INVALID ✗"));
        
        // 5. Test với dữ liệu sai
        verifier.initVerify(keyPair.getPublic());
        verifier.update("Wrong data".getBytes());
        boolean isInvalid = verifier.verify(signature);
        
        System.out.println("✓ Verification with wrong data: " + (isInvalid ? "VALID ✓" : "INVALID ✗"));
        
        System.out.println("\n=== DEMO COMPLETED ===");
    }
}
```

#### 4. Chạy demo:
```powershell
# Build và chạy
gradle run

# Hoặc build JAR và chạy
gradle build
java -jar build/libs/BC-Demo-1.0.0.jar
```

**Expected Output:**
```
=== DEMO BOUNCY CASTLE ECDSA ===

✓ Bouncy Castle Provider registered
✓ EC Key Pair generated (secp256r1)
  Private Key: EC
  Public Key: EC

✓ Message signed:
  Message: Hello Bouncy Castle ECDSA!
  Signature: MEUCIQDxxxxx...

✓ Signature verification: VALID ✓
✓ Verification with wrong data: INVALID ✗

=== DEMO COMPLETED ===
```

---

### Option B: Tạo demo với Spring Boot

#### 1. Tạo Spring Boot project:
```powershell
# Hoặc dùng Spring Initializr: https://start.spring.io
```

#### 2. Thêm dependency trong `build.gradle`:
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.bouncycastle:bcprov-jdk18on:1.77'
    implementation 'org.bouncycastle:bcpkix-jdk18on:1.77'
}
```

#### 3. Tạo REST Controller:
```java
package com.example.demo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.bind.annotation.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    @PostMapping("/sign")
    public Map<String, String> signData(@RequestBody Map<String, String> request) 
            throws Exception {
        String message = request.get("message");
        
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = keyGen.generateKeyPair();
        
        // Sign
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(message.getBytes());
        byte[] signature = signer.sign();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("signature", Base64.getEncoder().encodeToString(signature));
        response.put("algorithm", "SHA256withECDSA");
        response.put("curve", "secp256r1");
        
        return response;
    }
    
    @GetMapping("/algorithms")
    public Map<String, Object> getAlgorithms() {
        Map<String, Object> response = new HashMap<>();
        
        Provider bc = Security.getProvider("BC");
        Set<String> algorithms = new HashSet<>();
        
        bc.getServices().forEach(service -> {
            if (service.getType().equals("Signature")) {
                algorithms.add(service.getAlgorithm());
            }
        });
        
        response.put("provider", "BouncyCastle");
        response.put("version", bc.getVersionStr());
        response.put("signatureAlgorithms", algorithms);
        
        return response;
    }
}
```

#### 4. Chạy Spring Boot:
```powershell
.\gradlew bootRun

# Hoặc
mvn spring-boot:run
```

#### 5. Test API:
```powershell
# POST request
curl -X POST http://localhost:8080/api/crypto/sign `
  -H "Content-Type: application/json" `
  -d '{"message": "Hello ECDSA"}'

# GET algorithms
curl http://localhost:8080/api/crypto/algorithms
```

---

## 🔍 CÁCH XEM EXAMPLES TRONG DỰ ÁN

Dự án có rất nhiều test cases là examples tuyệt vời:

### 1. ECDSA Examples:
```powershell
# Tìm file
explorer test\src\test\java\org\bouncycastle\jce\provider\test

# Các file quan trọng:
# - ECDSATest.java
# - ECTest.java
# - ECIESTest.java
```

### 2. Certificate Examples:
```powershell
explorer pkix\src\test\java\org\bouncycastle\cert\test
# - CertTest.java
# - X509CertificateTest.java
```

### 3. OpenPGP Examples:
```powershell
explorer pg\src\test\java\org\bouncycastle\openpgp\test
# - PGPKeyRingTest.java
# - PGPSignatureTest.java
```

### 4. Chạy một example cụ thể:
```powershell
# Xem code trong test file
code test\src\test\java\org\bouncycastle\jce\provider\test\ECDSATest.java

# Chạy test đó
.\gradlew :prov:test --tests "ECDSATest"
```

---

## 📊 BENCHMARKING (PERFORMANCE TESTING)

Nếu muốn test performance:

```powershell
# Chạy JMH benchmarks
.\gradlew :core:run
```

---

## 🎓 HỌC TẬP VÀ NGHIÊN CỨU

### 1. Đọc documentation:
```powershell
# Mở docs
start docs\index.html
start docs\specifications.html
```

### 2. Xem source code:
```powershell
# Core crypto algorithms
code core\src\main\java\org\bouncycastle\crypto

# ECDSA implementation
code prov\src\main\java\org\bouncycastle\jcajce\provider\asymmetric\ec

# X.509 certificates
code pkix\src\main\java\org\bouncycastle\cert
```

### 3. Chạy specific tests để học:
```powershell
# Test ECDSA signing
.\gradlew :prov:test --tests "*ECDSA*"

# Test certificate generation
.\gradlew :pkix:test --tests "*Cert*"

# Test với verbose output
.\gradlew test --info
```

---

## 🐛 TROUBLESHOOTING

### Lỗi: "BC_JDK8 environment variable not found"

**Giải pháp:**
```powershell
# Set lại biến môi trường
$env:BC_JDK8="C:\Program Files\Java\jdk1.8.0_xxx"
$env:BC_JDK11="C:\Program Files\Java\jdk-11.x.x"
$env:BC_JDK17="C:\Program Files\Java\jdk-17.x.x"
$env:BC_JDK21="C:\Program Files\Java\jdk-21.x.x"

# Restart PowerShell và thử lại
```

### Lỗi: "JAVA_HOME is not set"

**Giải pháp:**
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17.x.x"

# Kiểm tra
java -version
```

### Lỗi: "gradlew: command not found"

**Giải pháp:**
```powershell
# Windows, dùng:
.\gradlew.bat clean build

# Hoặc cho quyền execute:
.\gradlew clean build
```

### Lỗi: "Tests failed"

**Giải pháp:**
```powershell
# Bỏ qua tests và chỉ build
.\gradlew clean build -x test

# Hoặc xem log chi tiết
.\gradlew test --stacktrace --info
```

### Lỗi: "Out of memory"

**Giải pháp:**
```powershell
# Tăng memory cho Gradle
$env:GRADLE_OPTS="-Xmx4g -XX:MaxPermSize=512m"

# Hoặc tạo file gradle.properties:
echo "org.gradle.jvmargs=-Xmx4g" > gradle.properties
```

### Build quá lâu?

**Giải pháp:**
```powershell
# Build song song
.\gradlew build --parallel --max-workers=4

# Hoặc build chỉ một module
.\gradlew :core:build
.\gradlew :prov:build
```

---

## 📚 RESOURCES

### Documentation:
- [Bouncy Castle Official](https://www.bouncycastle.org)
- [API Docs](https://www.bouncycastle.org/docs/docs1.5on/index.html)
- [User Guide](https://www.bouncycastle.org/specifications.html)

### Examples:
- Test folder: `test/src/test/java/`
- Provider tests: `prov/src/test/java/`
- PKIX tests: `pkix/src/test/java/`

### Standards:
- [RFC 6979 - Deterministic ECDSA](https://tools.ietf.org/html/rfc6979)
- [RFC 5280 - X.509 PKI](https://tools.ietf.org/html/rfc5280)
- [RFC 8446 - TLS 1.3](https://tools.ietf.org/html/rfc8446)

---

## 🎯 TÓM TẮT NHANH

```powershell
# 1. Set environment
$env:BC_JDK8="C:\path\to\jdk8"
$env:BC_JDK11="C:\path\to\jdk11"
$env:BC_JDK17="C:\path\to\jdk17"
$env:BC_JDK21="C:\path\to\jdk21"
$env:JAVA_HOME="C:\path\to\jdk17"

# 2. Build
cd C:\Users\Minh\Desktop\Nckk-ecdsa\NCKK-ECDSA
.\gradlew clean build

# 3. Run tests (để xem hoạt động)
.\gradlew test

# 4. Tìm JAR files
ls *\build\libs\*.jar

# 5. Sử dụng trong project khác
# Copy JAR files vào project của bạn và import
```

---

## ✅ CHECKLIST

Trước khi chạy, đảm bảo:

- [ ] JDK 8, 11, 17, 21 đã cài đặt
- [ ] Biến môi trường BC_JDK* đã set
- [ ] JAVA_HOME trỏ đến JDK 17+
- [ ] Đã cd vào thư mục dự án
- [ ] Đã chạy `.\gradlew clean build`
- [ ] Build successful (không có errors)

---

**📝 Tài liệu này hướng dẫn chi tiết cách build, test và sử dụng Bouncy Castle**  
**📅 Ngày:** 2025-12-08  
**🔖 Phiên bản:** 1.0

---

## 💡 MẸO HAY

1. **Không cần 4 JDK?** Có thể skip bằng cách comment out validation trong `build.gradle`
2. **Muốn build nhanh?** Dùng `.\gradlew :core:build -x test`
3. **Học ECDSA?** Xem file `test/src/test/java/org/bouncycastle/jce/provider/test/ECDSATest.java`
4. **Cần examples?** Mọi test file đều là examples tuyệt vời!
5. **Debug?** Dùng `.\gradlew test --debug > log.txt`

Chúc bạn thành công! 🎉
