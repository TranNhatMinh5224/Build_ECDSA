# Hướng dẫn Fix Lỗi "Unsupported class file major version 67"

## 🔴 Lỗi

```
Unsupported class file major version 67
```

## ❓ Nguyên nhân

- **Class file major version 67** = **Java 23**
- Gradle 8.5 **KHÔNG hỗ trợ** Java 23
- Gradle 8.5 chỉ hỗ trợ: **Java 8, 11, 17, hoặc 21**

Hiện tại máy bạn đang dùng **Java 23** làm JAVA_HOME, nên Gradle không chạy được.

## ✅ Giải pháp

### Cách 1: Dùng Script Tự Động (Khuyến nghị)

```powershell
# Script này sẽ tự động tìm và set Java version đúng
.\fix-java-version.ps1

# Sau đó build
.\build-with-log.ps1
```

Hoặc dùng script kết hợp:

```powershell
.\build-with-correct-java.ps1
```

### Cách 2: Set JAVA_HOME Thủ Công

#### Bước 1: Tìm Java version phù hợp trên máy

```powershell
# Tìm tất cả Java đã cài
Get-ChildItem "C:\Program Files\Java" | Select-Object Name
Get-ChildItem "C:\Program Files (x86)\Java" | Select-Object Name
```

Tìm các folder như:
- `jdk-21` hoặc `jdk1.8.0_xxx` (Java 8)
- `jdk-11.0.x`
- `jdk-17`
- `jdk-21`

#### Bước 2: Set JAVA_HOME tạm thời (cho session hiện tại)

```powershell
# Ví dụ với Java 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify
java -version
```

#### Bước 3: Set các biến BC_JDK* (bắt buộc cho project này)

```powershell
# Set các biến môi trường (tạm thời)
$env:BC_JDK8 = "C:\Program Files\Java\jdk1.8.0_xxx"   # Nếu có
$env:BC_JDK11 = "C:\Program Files\Java\jdk-11.0.x"    # Nếu có
$env:BC_JDK17 = "C:\Program Files\Java\jdk-17"         # Nếu có
$env:BC_JDK21 = "C:\Program Files\Java\jdk-21"        # Nếu có
```

**Lưu ý**: Chỉ set các biến cho Java version bạn thực sự có trên máy.

#### Bước 4: Verify

```powershell
# Kiểm tra Java version
java -version
# Phải hiển thị Java 8, 11, 17 hoặc 21, KHÔNG phải Java 23

# Kiểm tra biến môi trường
echo $env:BC_JDK21
echo $env:JAVA_HOME
```

#### Bước 5: Build lại

```powershell
.\build-with-log.ps1
```

### Cách 3: Set Vĩnh Viễn (Windows)

1. Mở **System Properties**:
   - Nhấn `Win + R`
   - Gõ `sysdm.cpl` và Enter
   - Tab **Advanced** → **Environment Variables**

2. Thêm vào **User variables** hoặc **System variables**:
   - `JAVA_HOME` = `C:\Program Files\Java\jdk-21` (hoặc version bạn có)
   - `BC_JDK8` = `C:\Program Files\Java\jdk1.8.0_xxx` (nếu có)
   - `BC_JDK11` = `C:\Program Files\Java\jdk-11.0.x` (nếu có)
   - `BC_JDK17` = `C:\Program Files\Java\jdk-17` (nếu có)
   - `BC_JDK21` = `C:\Program Files\Java\jdk-21` (nếu có)

3. Cập nhật PATH:
   - Thêm `%JAVA_HOME%\bin` vào PATH (nếu chưa có)

4. Mở lại PowerShell/Command Prompt và verify:
   ```cmd
   java -version
   ```

## 🔍 Kiểm tra Java Version

### Xem Java version hiện tại:
```powershell
java -version
```

### Xem JAVA_HOME:
```powershell
echo $env:JAVA_HOME
```

### Xem các biến BC_JDK*:
```powershell
echo $env:BC_JDK8
echo $env:BC_JDK11
echo $env:BC_JDK17
echo $env:BC_JDK21
```

## 📋 Class File Major Version Reference

| Java Version | Major Version |
|--------------|---------------|
| Java 8       | 52            |
| Java 11      | 55            |
| Java 17      | 61            |
| Java 21      | 65            |
| Java 23      | 67            |

**Gradle 8.5 hỗ trợ**: Java 8, 11, 17, 21 (major version ≤ 65)

## ⚠️ Lưu ý

1. **Java 23 không được hỗ trợ** bởi Gradle 8.5
2. Phải set **cả JAVA_HOME và BC_JDK*** để build được
3. Nếu không có Java 8/11/17/21, cần **cài đặt** một trong các version này
4. Có thể cài nhiều Java version cùng lúc, chỉ cần set đúng JAVA_HOME

## 🚀 Quick Fix (Copy & Paste)

Nếu bạn có Java 21 tại `C:\Program Files\Java\jdk-21`:

```powershell
# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Set BC_JDK21 (bắt buộc)
$env:BC_JDK21 = "C:\Program Files\Java\jdk-21"

# Verify
java -version

# Build
.\build-with-log.ps1
```

## 📞 Nếu vẫn lỗi

1. Kiểm tra Java version: `java -version` phải ≤ 21
2. Kiểm tra JAVA_HOME: `echo $env:JAVA_HOME`
3. Kiểm tra BC_JDK*: `echo $env:BC_JDK21`
4. Xem log file trong `build_logs/` để biết chi tiết lỗi








