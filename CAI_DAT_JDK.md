# Hướng dẫn Cài Đặt JDK để Build Project

## 🔴 Vấn đề hiện tại

- Bạn chỉ có **JRE 8** (Java Runtime Environment)
- Gradle cần **JDK** (Java Development Kit) để build
- JDK khác JRE: JDK có thêm compiler (`javac.exe`)

## ✅ Giải pháp: Cài JDK

### Cách 1: Cài JDK 8 (Khuyến nghị cho project này)

#### Option A: Eclipse Temurin (Miễn phí, Khuyến nghị)

1. **Download JDK 8:**
   - Truy cập: https://adoptium.net/temurin/releases/?version=8
   - Chọn:
     - **Version**: 8 (LTS)
     - **Operating System**: Windows
     - **Architecture**: x64
     - **Package Type**: JDK
   - Click **Download**

2. **Cài đặt:**
   - Chạy file `.msi` vừa download
   - Cài vào: `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot`
   - ✅ Tích chọn "Add to PATH" (nếu có option)

3. **Set biến môi trường:**
   ```powershell
   # Tìm đường dẫn JDK vừa cài (thường là)
   $jdk8Path = "C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot"
   
   # Set tạm thời
   $env:JAVA_HOME = $jdk8Path
   $env:PATH = "$jdk8Path\bin;$env:PATH"
   
   # Set BC_JDK8 (bắt buộc)
   $env:BC_JDK8 = $jdk8Path
   ```

#### Option B: Oracle JDK 8 (Cần đăng ký Oracle Account)

1. **Download:**
   - https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
   - Cần đăng nhập Oracle Account (miễn phí)

2. **Cài đặt và set biến tương tự**

### Cách 2: Cài JDK 21 (Hiện đại hơn, cũng được)

1. **Download JDK 21:**
   - https://adoptium.net/temurin/releases/?version=21
   - Chọn Windows x64 JDK

2. **Cài đặt và set:**
   ```powershell
   $jdk21Path = "C:\Program Files\Eclipse Adoptium\jdk-21.0.xxx-hotspot"
   $env:JAVA_HOME = $jdk21Path
   $env:PATH = "$jdk21Path\bin;$env:PATH"
   $env:BC_JDK21 = $jdk21Path
   ```

## 🔍 Kiểm tra sau khi cài

```powershell
# Kiểm tra JDK (phải có javac.exe)
java -version
javac -version

# Kiểm tra JAVA_HOME
echo $env:JAVA_HOME
Test-Path "$env:JAVA_HOME\bin\javac.exe"  # Phải là True
```

## 🚀 Sau khi cài JDK

1. **Chạy script fix:**
   ```powershell
   .\QUICK_FIX_JAVA.ps1
   ```

2. **Hoặc set thủ công:**
   ```powershell
   # Tìm đường dẫn JDK vừa cài
   $jdkPath = "C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot"
   
   # Set JAVA_HOME
   $env:JAVA_HOME = $jdkPath
   $env:PATH = "$jdkPath\bin;$env:PATH"
   
   # Set BC_JDK* (bắt buộc)
   $env:BC_JDK8 = $jdkPath
   $env:BC_JDK11 = $jdkPath  # Tạm thời dùng JDK 8
   $env:BC_JDK17 = $jdkPath
   $env:BC_JDK21 = $jdkPath
   
   # Clear Gradle cache
   Remove-Item "$env:USERPROFILE\.gradle\caches\8.5\scripts" -Recurse -Force -ErrorAction SilentlyContinue
   
   # Build
   .\build-with-log.ps1
   ```

## 📝 Set Vĩnh Viễn (Windows)

1. Mở **System Properties**:
   - `Win + R` → `sysdm.cpl` → Tab **Advanced** → **Environment Variables**

2. Thêm vào **User variables**:
   - `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot`
   - `BC_JDK8` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot`
   - `BC_JDK11` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot` (tạm thời)
   - `BC_JDK17` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot` (tạm thời)
   - `BC_JDK21` = `C:\Program Files\Eclipse Adoptium\jdk-8.0.xxx-hotspot` (tạm thời)

3. Cập nhật **PATH**:
   - Thêm `%JAVA_HOME%\bin` vào PATH

4. Mở lại PowerShell và verify

## ⚠️ Lưu ý

- **JDK ≠ JRE**: JDK có `javac.exe` (compiler), JRE chỉ có `java.exe` (runtime)
- Gradle cần JDK để compile code
- Có thể cài nhiều JDK version cùng lúc, chỉ cần set đúng JAVA_HOME

## 🆘 Nếu vẫn lỗi

1. Kiểm tra JDK đã cài đúng chưa:
   ```powershell
   Test-Path "$env:JAVA_HOME\bin\javac.exe"
   ```

2. Clear Gradle cache:
   ```powershell
   Remove-Item "$env:USERPROFILE\.gradle\caches" -Recurse -Force
   ```

3. Xem log file trong `build_logs/` để biết lỗi chi tiết








