# Hướng dẫn Build với Logging

Hệ thống logging này giúp bạn ghi lại tất cả lỗi khi build để dễ dàng debug và tìm nguyên nhân.

## 📋 Mục lục

1. [Cách sử dụng](#cách-sử-dụng)
2. [Xem log](#xem-log)
3. [Các lỗi thường gặp](#các-lỗi-thường-gặp)
4. [Troubleshooting](#troubleshooting)

## 🚀 Cách sử dụng

### Cách 1: Dùng PowerShell Script (Khuyến nghị)

```powershell
# Build bình thường
.\build-with-log.ps1

# Build với clean
.\build-with-log.ps1 -Clean

# Build task cụ thể
.\build-with-log.ps1 -Task "test"

# Build với clean và task cụ thể
.\build-with-log.ps1 -Task "build" -Clean
```

**Lưu ý**: Nếu gặp lỗi "execution of scripts is disabled", chạy:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Cách 2: Dùng Batch Script

```cmd
# Build bình thường
build-with-log.bat

# Build task cụ thể
build-with-log.bat test
```

### Cách 3: Dùng Gradle trực tiếp với logging

```cmd
# Build với stacktrace và info
gradlew.bat build --stacktrace --info > build.log 2>&1

# Build với debug (rất chi tiết)
gradlew.bat build --stacktrace --debug > build.log 2>&1
```

## 📁 Xem log

### Vị trí log files

Logs được lưu trong thư mục `build_logs/`:
- `build_YYYYMMDD_HHMMSS.log` - Full log (tất cả output)
- `build_errors_YYYYMMDD_HHMMSS.log` - Chỉ lỗi (errors và warnings)

### Xem log mới nhất bằng Gradle task

```cmd
gradlew.bat showBuildLog
```

### Xem log thủ công

1. Mở thư mục `build_logs/`
2. Tìm file log mới nhất (theo timestamp)
3. Mở bằng text editor

### Tìm lỗi trong log

Trong file log, tìm các từ khóa:
- `[ERROR]` - Lỗi nghiêm trọng
- `[WARN]` - Cảnh báo
- `FAILURE` - Build thất bại
- `Exception` - Exception stack trace
- `Compilation failed` - Lỗi compile

## 🔧 Clean logs cũ

```cmd
gradlew.bat cleanLogs
```

Hoặc xóa thủ công thư mục `build_logs/`

## ❌ Các lỗi thường gặp

### 1. Lỗi JDK Environment Variables

**Lỗi:**
```
Looking for JDK ENV 'BC_JDK8' but found null
```

**Nguyên nhân**: Thiếu biến môi trường JDK

**Giải pháp**:
1. Set các biến môi trường:
   - `BC_JDK8` - Đường dẫn đến JDK 8
   - `BC_JDK11` - Đường dẫn đến JDK 11
   - `BC_JDK17` - Đường dẫn đến JDK 17
   - `BC_JDK21` - Đường dẫn đến JDK 21

2. PowerShell (tạm thời):
```powershell
$env:BC_JDK8 = "C:\Program Files\Java\jdk1.8.0_xxx"
$env:BC_JDK11 = "C:\Program Files\Java\jdk-11.0.x"
$env:BC_JDK17 = "C:\Program Files\Java\jdk-17"
$env:BC_JDK21 = "C:\Program Files\Java\jdk-21"
```

3. Windows (vĩnh viễn):
   - Mở "Environment Variables" trong System Properties
   - Thêm các biến trên vào User variables hoặc System variables

### 2. Lỗi Compile Java

**Lỗi trong log:**
```
error: cannot find symbol
error: package does not exist
error: incompatible types
```

**Nguyên nhân**: 
- Import sai
- Class không tồn tại
- Type mismatch

**Giải pháp**:
1. Xem chi tiết trong log file (file nào, dòng nào)
2. Kiểm tra import statements
3. Kiểm tra dependencies trong `build.gradle`

### 3. Lỗi Dependency

**Lỗi trong log:**
```
Could not resolve all files for configuration
Could not find artifact
```

**Nguyên nhân**: Dependency không tìm thấy hoặc network issue

**Giải pháp**:
1. Kiểm tra internet connection
2. Kiểm tra Maven repository trong `build.gradle`
3. Thử clean và rebuild:
```cmd
gradlew.bat clean build --refresh-dependencies
```

### 4. Lỗi Out of Memory

**Lỗi trong log:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Giải pháp**:
1. Tăng heap size trong `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g
```

2. Hoặc set khi build:
```cmd
gradlew.bat build -Dorg.gradle.jvmargs="-Xmx4g"
```

### 5. Lỗi Permission Denied

**Lỗi trong log:**
```
Access is denied
Permission denied
```

**Giải pháp**:
1. Chạy terminal/command prompt với quyền Administrator
2. Kiểm tra quyền truy cập file/folder

## 🔍 Troubleshooting

### Log quá lớn

Nếu log file quá lớn (>100MB), có thể:
1. Xóa logs cũ: `gradlew.bat cleanLogs`
2. Build với level thấp hơn (bỏ `--info` hoặc `--debug`)

### Không thấy log file

1. Kiểm tra quyền ghi vào thư mục `build_logs/`
2. Kiểm tra disk space
3. Xem console output có lỗi gì không

### Log không đầy đủ

1. Thêm `--stacktrace` và `--info` vào Gradle command
2. Kiểm tra cả stdout và stderr đều được redirect

### Tìm lỗi nhanh

Trong PowerShell:
```powershell
# Tìm tất cả ERROR trong log mới nhất
Get-Content build_logs\build_*.log | Select-String "ERROR"

# Tìm exception stack trace
Get-Content build_logs\build_*.log | Select-String "Exception" -Context 5,10
```

## 📝 Ví dụ log

### Log thành công:
```
[2024-01-15 10:30:45.123] [INFO] BUILD THÀNH CÔNG!
[2024-01-15 10:30:45.124] [INFO] Build duration: 120.5 seconds
```

### Log thất bại:
```
[2024-01-15 10:30:45.123] [ERROR] BUILD THẤT BẠI với exit code: 1
[2024-01-15 10:30:45.124] [ERROR] Compilation failed for [core/src/main/java]
[2024-01-15 10:30:45.125] [ERROR] error: cannot find symbol: class CustomECGenerator
```

## 💡 Tips

1. **Luôn xem log file sau khi build thất bại** - Console có thể không hiển thị đầy đủ
2. **Tìm từ khóa ERROR trước** - Để nhanh chóng tìm lỗi
3. **Xem stack trace đầy đủ** - Để biết nguyên nhân gốc rễ
4. **So sánh với log thành công** - Để biết điểm khác biệt
5. **Giữ logs cũ** - Để so sánh khi cần

## 📞 Hỗ trợ

Nếu vẫn gặp vấn đề:
1. Xem log file chi tiết
2. Kiểm tra README.md và HUONG_DAN_CHAY_DU_AN.md
3. Tìm kiếm lỗi trên Google với message cụ thể từ log








