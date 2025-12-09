# 🤔 TẠI SAO DỰ ÁN CÓ NHIỀU PHIÊN BẢN JAVA?

## 📌 TÓM TẮT NHANH

Dự án Bouncy Castle yêu cầu **4 phiên bản Java** (JDK 8, 11, 17, 21) vì:

1. ✅ **Multi-Release JAR (MR-JAR)** - Tính năng từ Java 9
2. ✅ **Tương thích ngược** - Hỗ trợ từ Java 8 đến Java 21+
3. ✅ **Tối ưu hóa** - Dùng tính năng mới của từng phiên bản
4. ✅ **API Evolution** - Java thay đổi API qua các phiên bản

---

## 🎯 GIẢI THÍCH CHI TIẾT

### 1️⃣ **MULTI-RELEASE JAR (MR-JAR)**

Đây là một tính năng từ **Java 9** (JEP 238) cho phép:

```
bcprov-jdk18on.jar
│
├── org/bouncycastle/...          ← Code cho Java 8 (base)
│   └── crypto/
│       └── ECDSASigner.class
│
└── META-INF/
    └── versions/
        ├── 9/                     ← Code tối ưu cho Java 9+
        │   └── org/bouncycastle/...
        ├── 11/                    ← Code tối ưu cho Java 11+
        │   └── org/bouncycastle/...
        ├── 15/                    ← Code tối ưu cho Java 15+
        │   └── org/bouncycastle/...
        └── 21/                    ← Code tối ưu cho Java 21+
            └── org/bouncycastle/...
```

**Cách hoạt động:**
```
┌─────────────────────────────────────────────────┐
│  Khi chạy trên Java 8:                          │
│  → JVM load code từ thư mục gốc                 │
│                                                 │
│  Khi chạy trên Java 11:                         │
│  → JVM load code từ META-INF/versions/11/       │
│  → Nếu không có, fallback về thư mục gốc        │
│                                                 │
│  Khi chạy trên Java 21:                         │
│  → JVM load code từ META-INF/versions/21/       │
│  → Nếu không có, fallback về version thấp hơn   │
└─────────────────────────────────────────────────┘
```

---

### 2️⃣ **TẠI SAO CẦN NHIỀU PHIÊN BẢN?**

#### A. **API Changes trong Java**

Java thay đổi và deprecated một số API qua các phiên bản:

```java
// ❌ Java 8 - API cũ
new Integer(42)                    // Deprecated từ Java 9
new Date()                         // Có vấn đề về timezone
Thread.stop()                      // Deprecated, không an toàn

// ✅ Java 9+ - API mới
Integer.valueOf(42)                // Tối ưu hơn
Instant.now()                      // Thread-safe, tốt hơn
Thread.interrupt()                 // An toàn hơn
```

#### B. **Tính năng mới trong từng phiên bản**

**Java 8 (Base - năm 2014):**
```java
// Lambda expressions
list.forEach(item -> process(item));

// Stream API
list.stream().filter(x -> x > 0).collect(Collectors.toList());
```

**Java 9+ (2017):**
```java
// Module System (Project Jigsaw)
module org.bouncycastle.provider {
    exports org.bouncycastle.crypto;
    requires java.base;
}

// Private methods in interfaces
interface Crypto {
    private void helperMethod() { ... }
}

// VarHandle (thay thế Unsafe)
VarHandle vh = MethodHandles.lookup()
    .findVarHandle(MyClass.class, "field", int.class);
```

**Java 11+ (LTS - 2018):**
```java
// String methods mới
var text = "  hello  ";
text.isBlank();
text.strip();
text.lines();

// New HTTP Client
HttpClient client = HttpClient.newHttpClient();

// Nest-based access control
// Performance improvements
```

**Java 15+ (2020):**
```java
// Text Blocks
String json = """
    {
        "name": "John",
        "age": 30
    }
    """;

// Sealed classes (preview)
public sealed class Shape 
    permits Circle, Rectangle { }
```

**Java 17+ (LTS - 2021):**
```java
// Pattern Matching for switch
String formatted = switch (obj) {
    case Integer i -> String.format("int %d", i);
    case Long l    -> String.format("long %d", l);
    case String s  -> String.format("String %s", s);
    default        -> obj.toString();
};

// Sealed classes (finalized)
```

**Java 21+ (LTS - 2023):**
```java
// Virtual Threads (Project Loom)
Thread.startVirtualThread(() -> {
    // Lightweight threads
});

// Pattern Matching for switch (finalized)
// Record Patterns
// Sequenced Collections
```

---

### 3️⃣ **VÍ DỤ THỰC TẾ TRONG BOUNCY CASTLE**

#### Cấu trúc source code:

```
prov/src/main/
├── java/                          ← Java 8 compatible (BASE)
│   └── org/bouncycastle/
│       └── jce/provider/
│           └── BouncyCastleProvider.java
│
├── jdk1.9/                        ← Java 9 specific code
│   └── org/bouncycastle/
│       └── jcajce/provider/
│           └── ProviderConfiguration.java
│
├── jdk1.11/                       ← Java 11 specific code
│   └── org/bouncycastle/
│
├── jdk1.15/                       ← Java 15 specific code
│   └── org/bouncycastle/
│
└── jdk21/                         ← Java 21 specific code
    └── org/bouncycastle/
```

#### Code khác nhau giữa các phiên bản:

**Java 8 (base):**
```java
// Sử dụng Unsafe (deprecated)
sun.misc.Unsafe unsafe = ...;
unsafe.compareAndSwapInt(obj, offset, expected, value);

// Anonymous inner classes
Runnable r = new Runnable() {
    @Override
    public void run() {
        doWork();
    }
};
```

**Java 9+ (jdk1.9):**
```java
// Sử dụng VarHandle (replacement cho Unsafe)
VarHandle VH_INT = MethodHandles.lookup()
    .findVarHandle(MyClass.class, "field", int.class);
VH_INT.compareAndSet(obj, expected, value);

// Lambda
Runnable r = () -> doWork();

// Module system
module org.bouncycastle.provider {
    exports org.bouncycastle.jce.provider;
}
```

**Java 11+ (jdk1.11):**
```java
// Nest-based access control
// Tối ưu hóa access giữa nested classes
class Outer {
    private int field;
    
    class Inner {
        void access() {
            // Java 11+: Direct field access (faster)
            // Java 8: Qua synthetic bridge method (slower)
            int x = field;
        }
    }
}
```

**Java 21+ (jdk21):**
```java
// Virtual threads cho crypto operations
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // Parallel crypto operations
        performExpensiveCrypto();
    });
}

// Pattern matching
Object result = performCrypto();
if (result instanceof byte[] bytes) {
    process(bytes);
}
```

---

### 4️⃣ **LỢI ÍCH CỦA MULTI-VERSION**

#### ✅ **Backward Compatibility (Tương thích ngược)**
```
Một JAR duy nhất chạy được trên:
- Java 8 applications (2014)
- Java 11 applications (2018)
- Java 17 applications (2021)
- Java 21 applications (2023)
- Java 25+ applications (future)
```

#### ✅ **Performance Optimization**
```java
// Java 8: Chậm hơn
synchronized (lock) {
    // Critical section
}

// Java 9+: Nhanh hơn với VarHandle
VarHandle vh = ...;
vh.getAcquire(obj);  // Optimized memory access
```

#### ✅ **Security Enhancements**
```java
// Java 8: Limited crypto APIs
// Java 11+: Modern crypto APIs
// Java 17+: Better random number generation
// Java 21+: Quantum-resistant algorithms support
```

#### ✅ **Future-Proof**
```
Khi Java 25 ra đời với tính năng mới:
→ Chỉ cần thêm jdk25/ folder
→ Không cần maintain nhiều JAR khác nhau
```

---

### 5️⃣ **BUILD PROCESS**

#### Gradle build với multiple JDK:

```groovy
// prov/build.gradle
sourceSets {
    main {
        java {
            srcDirs '../core/src/main/java'  // Java 8 base
        }
    }
    
    java9 {
        java {
            srcDirs = ['src/main/jdk1.9']    // Java 9 code
        }
    }
    
    java11 {
        java {
            srcDirs = ['src/main/jdk1.11']   // Java 11 code
        }
    }
    
    java15 {
        java {
            srcDirs = ['src/main/jdk1.15']   // Java 15 code
        }
    }
    
    java21 {
        java {
            srcDirs = ['src/main/jdk21']     // Java 21 code
        }
    }
}
```

#### Compilation steps:

```bash
# Step 1: Compile với JDK 8
BC_JDK8 compile → classes/

# Step 2: Compile với JDK 11
BC_JDK11 compile → classes/META-INF/versions/11/

# Step 3: Compile với JDK 17
BC_JDK17 compile → classes/META-INF/versions/17/

# Step 4: Compile với JDK 21
BC_JDK21 compile → classes/META-INF/versions/21/

# Step 5: Package tất cả vào 1 JAR
jar cvf bcprov-jdk18on.jar classes/
```

---

### 6️⃣ **SO SÁNH VỚI CÁCH KHÁC**

#### ❌ **Cách cũ (trước Java 9):**
```
Phải maintain nhiều JARs:
- bcprov-jdk15.jar    (for Java 1.5-1.7)
- bcprov-jdk16.jar    (for Java 1.6-1.7)
- bcprov-jdk18.jar    (for Java 1.8)
- bcprov-jdk11.jar    (for Java 11+)
- bcprov-jdk17.jar    (for Java 17+)

→ Phức tạp cho developers
→ Dễ nhầm lẫn version
→ Tốn storage space
```

#### ✅ **Cách mới (Multi-Release JAR):**
```
Chỉ cần 1 JAR duy nhất:
- bcprov-jdk18on.jar  (works on Java 8+)

→ Đơn giản hơn
→ Tự động chọn version phù hợp
→ Tiết kiệm không gian
```

---

### 7️⃣ **TESTING VỚI NHIỀU VERSIONS**

#### Test tasks trong Gradle:

```groovy
// Test trên Java 8 (default)
task test

// Test trên Java 11
task test11 {
    executable = "$BC_JDK11/bin/java"
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

// Test trên Java 17
task test17 {
    executable = "$BC_JDK17/bin/java"
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// Test trên Java 21
task test21 {
    executable = "$BC_JDK21/bin/java"
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

#### Chạy tất cả tests:
```bash
./gradlew clean build test11 test17 test21
```

---

### 8️⃣ **VÍ DỤ THỰC TẾ VỀ SỰ KHÁC BIỆT**

#### Security Provider Registration:

**Java 8:**
```java
import sun.security.ec.SunEC;  // Internal API

public class BouncyCastleProvider extends Provider {
    public BouncyCastleProvider() {
        super("BC", 1.77, "Bouncy Castle Provider");
        // Register algorithms
    }
}
```

**Java 9+:**
```java
// Không thể dùng sun.* package (module system)
// Phải dùng public APIs

public class BouncyCastleProvider extends Provider {
    public BouncyCastleProvider() {
        super("BC", 1.77, "Bouncy Castle Provider");
        // Use java.security.spec.* instead
    }
}
```

#### Random Number Generation:

**Java 8:**
```java
SecureRandom random = new SecureRandom();
// Uses SHA1PRNG by default
```

**Java 11+:**
```java
SecureRandom random = SecureRandom.getInstance("DRBG");
// Modern DRBG algorithm (NIST SP 800-90A)
```

**Java 17+:**
```java
SecureRandom random = SecureRandom.getInstance("DRBG",
    DrbgParameters.instantiation(256, RESEED_ONLY, null));
// More control over DRBG parameters
```

---

## 🎓 KẾT LUẬN

### Tại sao cần 4 JDK versions?

1. **BC_JDK8** - Compile base code (Java 8 compatible)
2. **BC_JDK11** - Compile Java 11+ optimizations
3. **BC_JDK17** - Compile Java 17+ optimizations  
4. **BC_JDK21** - Compile Java 21+ optimizations

### Lợi ích:

✅ **Một JAR** → chạy trên mọi Java version (8-21+)  
✅ **Tối ưu** → mỗi version Java dùng code tối ưu nhất  
✅ **Tương thích** → support cả Java cũ và mới  
✅ **Future-proof** → dễ thêm Java 25, 29... sau này  
✅ **Maintainable** → không cần maintain nhiều branches  

---

## 💡 GIẢI PHÁP NẾU BẠN CHƯA CÓ ĐỦ 4 JDK

### Option 1: Chỉ build với Java 17
```bash
# Bỏ qua validation
# Comment out phần check trong build.gradle

# Chỉ cần JDK 17
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
./gradlew build
```

### Option 2: Download thiếu JDK nào
```powershell
# Download từ:
# https://adoptium.net/ (Recommended)
# https://www.oracle.com/java/technologies/downloads/

# Cài đặt:
- JDK 8:  adoptium-8.jdk
- JDK 11: adoptium-11.jdk
- JDK 17: adoptium-17.jdk
- JDK 21: adoptium-21.jdk
```

### Option 3: Dùng Docker (easiest!)
```bash
# Build trong Docker container có sẵn multi-JDK
docker run -v ${PWD}:/work -w /work gradle:jdk17 gradle build
```

---

## 🔗 TÀI LIỆU THAM KHẢO

- [JEP 238: Multi-Release JAR Files](https://openjdk.org/jeps/238)
- [Java Version History](https://en.wikipedia.org/wiki/Java_version_history)
- [Oracle Java Documentation](https://docs.oracle.com/en/java/)
- [Bouncy Castle Documentation](https://www.bouncycastle.org/documentation.html)

---

📝 **Tóm lại:** Nhiều Java version = Một thư viện tốt hơn, nhanh hơn, tương thích hơn! 🚀
