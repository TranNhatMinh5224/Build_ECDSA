# 📊 PHÂN TÍCH DỰ ÁN BOUNCY CASTLE - NCKK-ECDSA

## 🎯 TỔNG QUAN DỰ ÁN

**Tên dự án:** Bouncy Castle Cryptography Package for Java (NCKK-ECDSA)  
**Mục đích:** Cung cấp thư viện mật mã học hoàn chỉnh cho Java với tập trung vào **ECDSA** (Elliptic Curve Digital Signature Algorithm)  
**Loại:** Library/Framework - Cryptography  
**Ngôn ngữ:** Java (100%)  
**Build tool:** Gradle  
**Giấy phép:** MIT X Consortium License & Apache License 2.0

---

## 📐 KIẾN TRÚC TỔNG THỂ

```
┌─────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                     │
│        (Ứng dụng sử dụng Bouncy Castle APIs)            │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   JCA/JCE PROVIDER                       │
│           (Java Crypto Architecture/Extension)           │
│                    [prov module]                         │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              SPECIALIZED MODULES                         │
├──────────────────────────────────────────────────────────┤
│  PKIX   │   TLS   │   PG   │   Mail   │   MLS          │
│ (X.509) │  (SSL)  │ (PGP)  │ (S/MIME) │ (Messaging)    │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   CORE CRYPTO LAYER                      │
│           (Lightweight Cryptographic APIs)               │
│                    [core module]                         │
│  ┌──────────┬──────────┬──────────┬──────────┐         │
│  │ Symmetric│Asymmetric│   Hash   │ Signature│         │
│  │   Cipher │   Cipher │Functions │Algorithms│         │
│  └──────────┴──────────┴──────────┴──────────┘         │
└──────────────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   UTILITY LAYER                          │
│           (ASN.1, Encoders, Helpers)                     │
│                    [util module]                         │
└──────────────────────────────────────────────────────────┘
```

---

## 🔄 FLOW XỬ LÝ CHÍNH

### 1. **FLOW KÝ SỐ (DIGITAL SIGNATURE)**

```
┌─────────────────────────────────────────────────────────────┐
│                    SIGNING FLOW (Ký số)                     │
└─────────────────────────────────────────────────────────────┘

Bước 1: Tạo Key Pair
┌──────────────────┐
│ KeyPairGenerator │──→ Generate EC Key Pair (P-256, P-384...)
└────────┬─────────┘
         │
         ▼
┌──────────────────────────┐
│  Private Key (d)         │  Secret, dùng để ký
│  Public Key (Q = d×G)    │  Public, dùng để verify
└────────┬─────────────────┘
         │
         ▼
Bước 2: Ký dữ liệu
┌──────────────────┐
│  Original Data   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Hash Function   │──→ SHA-256/SHA-384/SHA-512
│  (Digest)        │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Message Digest  │
│     (Hash)       │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────┐
│  ECDSA Sign Algorithm    │
│  Input: Hash + Private   │
│  Output: Signature (r,s) │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────┐
│  Signature       │──→ (r, s) pair
│  (r, s)          │
└──────────────────┘


Bước 3: Xác thực chữ ký
┌──────────────────┐         ┌──────────────────┐
│  Original Data   │         │  Signature       │
└────────┬─────────┘         │  (r, s)          │
         │                   └────────┬─────────┘
         ▼                            │
┌──────────────────┐                 │
│  Hash Function   │                 │
│  (Same as sign)  │                 │
└────────┬─────────┘                 │
         │                           │
         ▼                           ▼
┌────────────────────────────────────────┐
│    ECDSA Verify Algorithm              │
│    Input: Hash + Signature + Public    │
│    Output: true/false                  │
└────────┬───────────────────────────────┘
         │
         ▼
┌──────────────────┐
│  Verification    │──→ Valid / Invalid
│  Result          │
└──────────────────┘
```

### 2. **FLOW MÃ HÓA/GIẢI MÃ**

```
┌─────────────────────────────────────────────────────────────┐
│              ENCRYPTION/DECRYPTION FLOW                      │
└─────────────────────────────────────────────────────────────┘

[A] MÃ HÓA ĐỐI XỨNG (Symmetric)
─────────────────────────────────
Plaintext ──→ [AES/DES Cipher] ──→ Ciphertext
                    ↑
                Secret Key
                (256-bit)

[B] MÃ HÓA BẤT ĐỐI XỨNG (Asymmetric)
─────────────────────────────────────
1. Người gửi:
   Plaintext ──→ [RSA/EC Encrypt] ──→ Ciphertext
                      ↑
                  Public Key
                  (Receiver's)

2. Người nhận:
   Ciphertext ──→ [RSA/EC Decrypt] ──→ Plaintext
                       ↑
                   Private Key
                   (Receiver's)

[C] MÃ HÓA KẾT HỢP (Hybrid - TLS/SSL)
─────────────────────────────────────
1. Handshake: Trao đổi khóa bằng ECDH/RSA
2. Session: Mã hóa dữ liệu bằng AES
```

### 3. **FLOW QUẢN LÝ CHỨNG CHỈ (Certificate Management)**

```
┌─────────────────────────────────────────────────────────────┐
│           CERTIFICATE MANAGEMENT FLOW                        │
└─────────────────────────────────────────────────────────────┘

Bước 1: Tạo Certificate Request
┌──────────────────┐
│  Generate Keys   │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Create CSR      │──→ Certificate Signing Request
│  (PKCS#10)       │     (Thông tin + Public Key)
└────────┬─────────┘
         │
         ▼
Bước 2: CA ký chứng chỉ
┌──────────────────┐
│  CA Signs CSR    │──→ X.509 Certificate
└────────┬─────────┘
         │
         ▼
Bước 3: Sử dụng Certificate
┌──────────────────────────┐
│  Certificate Chain       │
│  ┌────────────────────┐  │
│  │ Root CA            │  │
│  └─────────┬──────────┘  │
│            │             │
│  ┌─────────▼──────────┐  │
│  │ Intermediate CA    │  │
│  └─────────┬──────────┘  │
│            │             │
│  ┌─────────▼──────────┐  │
│  │ End Entity Cert    │  │
│  └────────────────────┘  │
└──────────────────────────┘
         │
         ▼
Bước 4: Validation
┌──────────────────┐
│  Verify Chain    │──→ Check signature
│  Check Validity  │──→ Check expiry
│  Check Revocation│──→ OCSP/CRL
└──────────────────┘
```

---

## 🏗️ CẤU TRÚC CÂY THƯ MỤC CHI TIẾT

```
NCKK-ECDSA/
│
├── 📁 core/                          # Module cốt lõi
│   ├── build.gradle                  # Build config cho core
│   └── src/main/
│       ├── java/org/bouncycastle/
│       │   ├── crypto/               # Thuật toán mật mã cơ bản
│       │   │   ├── signers/          # ECDSA, DSA, RSA signers
│       │   │   │   ├── ECDSASigner.java
│       │   │   │   └── DSADigestSigner.java
│       │   │   ├── engines/          # Cipher engines
│       │   │   │   ├── AESEngine.java
│       │   │   │   ├── RSAEngine.java
│       │   │   │   └── DESedeEngine.java
│       │   │   ├── digests/          # Hash functions
│       │   │   │   ├── SHA256Digest.java
│       │   │   │   ├── SHA512Digest.java
│       │   │   │   └── SHA3Digest.java
│       │   │   ├── macs/             # MAC algorithms
│       │   │   ├── modes/            # Block cipher modes
│       │   │   ├── params/           # Crypto parameters
│       │   │   │   ├── ECKeyParameters.java
│       │   │   │   ├── ECPrivateKeyParameters.java
│       │   │   │   └── ECPublicKeyParameters.java
│       │   │   └── generators/       # Key generators
│       │   │       └── ECKeyPairGenerator.java
│       │   └── math/                 # Toán học cho crypto
│       │       └── ec/               # Elliptic Curve math
│       │           ├── ECPoint.java
│       │           ├── ECCurve.java
│       │           └── ECFieldElement.java
│       ├── j2me/                     # Java ME support
│       ├── jdk1.1/ đến jdk1.4/       # JDK version compatibility
│       └── resources/
│
├── 📁 prov/                          # JCA/JCE Provider
│   ├── build.gradle
│   └── src/main/
│       ├── java/org/bouncycastle/
│       │   ├── jce/provider/         # JCE implementation
│       │   │   ├── BouncyCastleProvider.java  # Main provider
│       │   │   ├── JCEECPublicKey.java
│       │   │   └── JCEECPrivateKey.java
│       │   └── jcajce/provider/      # JCA implementation
│       │       ├── asymmetric/
│       │       │   ├── ec/           # *** EC/ECDSA Implementation ***
│       │       │   │   ├── EC.java
│       │       │   │   ├── SignatureSpi.java     # ECDSA signatures
│       │       │   │   ├── KeyPairGeneratorSpi.java
│       │       │   │   └── KeyFactorySpi.java
│       │       │   ├── rsa/          # RSA implementation
│       │       │   ├── dsa/          # DSA implementation
│       │       │   └── compositesignatures/  # Hybrid signatures
│       │       │       ├── SignatureSpi.java
│       │       │       └── KeyPairGeneratorSpi.java
│       │       └── symmetric/        # Symmetric ciphers
│       └── jdk1.1/ đến jdk21/        # Multi-version support
│
├── 📁 pkix/                          # PKI & X.509
│   ├── build.gradle
│   └── src/main/
│       └── java/org/bouncycastle/
│           ├── cert/                 # Certificate generation
│           │   ├── X509CertificateHolder.java
│           │   ├── X509v3CertificateBuilder.java
│           │   └── jcajce/
│           │       └── JcaX509CertificateConverter.java
│           ├── cms/                  # Cryptographic Message Syntax
│           │   ├── CMSSignedData.java
│           │   ├── CMSSignedDataGenerator.java
│           │   └── SignerInformation.java
│           ├── operator/             # Operators for signing
│           │   ├── ContentSigner.java
│           │   ├── ContentVerifier.java
│           │   └── jcajce/
│           │       ├── JcaContentSignerBuilder.java
│           │       └── JcaContentVerifierProviderBuilder.java
│           ├── pkcs/                 # PKCS standards
│           │   ├── PKCS10CertificationRequest.java
│           │   └── PKCS12PfxPdu.java
│           ├── tsp/                  # Time Stamp Protocol
│           └── ocsp/                 # Online Certificate Status
│               ├── OCSPReq.java
│               └── OCSPResp.java
│
├── 📁 tls/                           # TLS/SSL
│   ├── build.gradle
│   └── src/main/
│       └── java/org/bouncycastle/
│           ├── tls/                  # TLS protocol
│           │   ├── TlsClient.java
│           │   ├── TlsServer.java
│           │   ├── SignatureAndHashAlgorithm.java
│           │   └── crypto/
│           │       ├── TlsSigner.java
│           │       ├── TlsVerifier.java
│           │       └── impl/
│           │           ├── jcajce/   # JCA-based TLS crypto
│           │           │   ├── JcaTlsCertificate.java
│           │           │   └── JcaTlsECDSASigner.java
│           │           └── bc/       # BC lightweight TLS crypto
│           └── jsse/                 # Java Secure Socket Extension
│               └── provider/
│                   ├── BouncyCastleJsseProvider.java
│                   └── SignatureSchemeInfo.java
│
├── 📁 pg/                            # OpenPGP
│   ├── build.gradle
│   └── src/main/
│       └── java/org/bouncycastle/
│           └── openpgp/              # PGP implementation
│               ├── PGPPublicKey.java
│               ├── PGPPrivateKey.java
│               ├── PGPSignature.java
│               └── operator/
│
├── 📁 mail/                          # S/MIME (Email security)
│   ├── build.gradle
│   └── src/main/
│       └── java/org/bouncycastle/
│           └── mail/smime/           # S/MIME implementation
│               ├── SMIMESignedGenerator.java
│               └── SMIMESignedParser.java
│
├── 📁 mls/                           # Messaging Layer Security
│   ├── build.gradle
│   └── src/main/
│
├── 📁 util/                          # Utilities
│   ├── build.gradle
│   └── src/main/
│       └── java/org/bouncycastle/
│           ├── asn1/                 # ASN.1 encoding/decoding
│           │   ├── eac/              # EAC (European Access Control)
│           │   │   └── ECDSAPublicKey.java
│           │   └── x509/             # X.509 structures
│           ├── util/                 # General utilities
│           │   ├── encoders/         # Base64, Hex encoders
│           │   └── io/
│           └── oer/its/              # OER encoding for ITS
│               └── ieee1609dot2/
│                   └── basetypes/
│                       ├── EcdsaP256Signature.java
│                       ├── EcdsaP384Signature.java
│                       └── Signature.java
│
├── 📁 test/                          # Test suite
│   ├── build.gradle
│   ├── README.md
│   ├── libs/                         # Test dependencies
│   │   ├── grpc-*.jar
│   │   ├── guava-*.jar
│   │   └── jna-*.jar
│   └── src/
│       ├── main/                     # Test utilities
│       └── test/                     # Test cases
│           └── java/org/bouncycastle/
│               ├── crypto/test/      # Crypto tests
│               │   ├── ECTest.java
│               │   └── ECDSATest.java
│               ├── jce/provider/test/  # JCE provider tests
│               └── cms/test/         # CMS tests
│
├── 📁 docs/                          # Documentation
│   ├── index.html
│   ├── releasenotes.html
│   └── specifications.html
│
├── 📁 scripts/                       # Build & deployment scripts
│   ├── jdk1.1ed.sh
│   ├── jdk1.2ed.sh
│   └── usejcecert.sh
│
├── 📁 config/                        # Configuration files
│   ├── checkstyle/
│   │   ├── checkstyle.xml
│   │   └── lib/
│   └── nohttp/
│       ├── checkstyle.xml
│       └── suppressions.xml
│
├── 📁 ci/                            # CI/CD scripts
│   ├── build_1_8.sh
│   ├── test_11.sh
│   ├── test_17.sh
│   └── test_21.sh
│
├── 📁 gradle/                        # Gradle wrapper
│   └── wrapper/
│
├── 📄 build.gradle                   # Root build file
├── 📄 settings.gradle                # Gradle settings
├── 📄 gradlew                        # Gradle wrapper (Unix)
├── 📄 gradlew.bat                    # Gradle wrapper (Windows)
├── 📄 gradle.properties              # Gradle properties
│
├── 📄 README.md                      # Hướng dẫn chính
├── 📄 LICENSE.html                   # Giấy phép
├── 📄 SECURITY.md                    # Security policy
└── 📄 CONTRIBUTORS.html              # Danh sách đóng góp
```

---

## 🎯 CHỨC NĂNG CHÍNH CỦA DỰ ÁN

### 1️⃣ **CHỮ KÝ SỐ (DIGITAL SIGNATURES)**

#### A. ECDSA (Elliptic Curve Digital Signature Algorithm) ⭐
**Vị trí:** `prov/src/main/java/org/bouncycastle/jcajce/provider/asymmetric/ec/`

**Chức năng:**
- ✅ Tạo cặp khóa EC (P-256, P-384, P-521, Brainpool curves)
- ✅ Ký dữ liệu với ECDSA
- ✅ Xác thực chữ ký ECDSA
- ✅ Hỗ trợ các hash: SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, SHA3
- ✅ ECDSA variants: Standard, Deterministic (RFC 6979), Plain-ECDSA

**Classes chính:**
```java
// Tạo key pair
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
keyGen.initialize(ecSpec);
KeyPair keyPair = keyGen.generateKeyPair();

// Ký
Signature sig = Signature.getInstance("SHA256withECDSA", "BC");
sig.initSign(keyPair.getPrivate());
sig.update(data);
byte[] signature = sig.sign();

// Verify
sig.initVerify(keyPair.getPublic());
sig.update(data);
boolean verified = sig.verify(signature);
```

#### B. RSA Signatures
- RSA-PSS (Probabilistic Signature Scheme)
- RSA PKCS#1 v1.5
- Composite signatures (RSA + ML-DSA)

#### C. DSA (Digital Signature Algorithm)
- Standard DSA
- ECDSA variants

#### D. EdDSA (Edwards-curve Digital Signature)
- Ed25519
- Ed448

#### E. Composite Signatures (Quantum-resistant)
- ML-DSA (Module-Lattice Digital Signature) + ECDSA
- Falcon + ECDSA
- Hybrid quantum-classical signatures

---

### 2️⃣ **MÃ HÓA (ENCRYPTION)**

#### A. Mã hóa đối xứng (Symmetric)
**Vị trí:** `core/src/main/java/org/bouncycastle/crypto/engines/`

**Thuật toán:**
- ✅ AES (128, 192, 256-bit)
- ✅ DES / 3DES
- ✅ Blowfish
- ✅ Twofish
- ✅ Camellia
- ✅ ChaCha20
- ✅ Serpent

**Modes:**
- CBC, CTR, GCM, CCM, ECB, CFB, OFB

#### B. Mã hóa bất đối xứng (Asymmetric)
**Thuật toán:**
- ✅ RSA
- ✅ ECIES (Elliptic Curve Integrated Encryption Scheme)
- ✅ ElGamal
- ✅ DSTU 4145 (Ukrainian standard)

---

### 3️⃣ **HASH FUNCTIONS (BĂM)**

**Vị trí:** `core/src/main/java/org/bouncycastle/crypto/digests/`

**Thuật toán:**
- ✅ MD5
- ✅ SHA-1
- ✅ SHA-2 family: SHA-224, SHA-256, SHA-384, SHA-512
- ✅ SHA-3 family: SHA3-224, SHA3-256, SHA3-384, SHA3-512
- ✅ SHAKE128, SHAKE256
- ✅ BLAKE2b, BLAKE2s
- ✅ RIPEMD160
- ✅ Whirlpool
- ✅ Tiger
- ✅ SM3 (Chinese standard)

---

### 4️⃣ **QUẢN LÝ CHỨNG CHỈ (PKI/X.509)**

**Vị trí:** `pkix/src/main/java/org/bouncycastle/cert/`

#### A. Tạo chứng chỉ
```java
// Certificate builder
X509v3CertificateBuilder certBuilder = 
    new X509v3CertificateBuilder(
        issuer,
        serialNumber,
        notBefore,
        notAfter,
        subject,
        subjectPublicKeyInfo
    );

// Thêm extensions
certBuilder.addExtension(
    Extension.keyUsage,
    true,
    new KeyUsage(KeyUsage.digitalSignature)
);

// Ký certificate
ContentSigner signer = 
    new JcaContentSignerBuilder("SHA256withECDSA")
        .build(caPrivateKey);
        
X509CertificateHolder certHolder = certBuilder.build(signer);
```

#### B. Certificate Validation
- ✅ Verify signature chain
- ✅ Check validity period
- ✅ Check revocation (CRL, OCSP)
- ✅ Path validation

#### C. PKCS Standards
- ✅ PKCS#10: Certificate Signing Request
- ✅ PKCS#12: Personal Information Exchange
- ✅ PKCS#7: Cryptographic Message Syntax

---

### 5️⃣ **TLS/SSL (TRANSPORT LAYER SECURITY)**

**Vị trí:** `tls/src/main/java/org/bouncycastle/tls/`

**Chức năng:**
- ✅ TLS 1.0, 1.1, 1.2, 1.3
- ✅ DTLS (Datagram TLS)
- ✅ JSSE Provider (Java Secure Socket Extension)
- ✅ Cipher suites với ECDSA/ECDH
- ✅ Server/Client implementation
- ✅ Custom cipher suites

**ECDSA trong TLS:**
```
Cipher Suites:
- TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
- TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
- TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256
```

---

### 6️⃣ **CMS (CRYPTOGRAPHIC MESSAGE SYNTAX)**

**Vị trí:** `pkix/src/main/java/org/bouncycastle/cms/`

**Chức năng:**
- ✅ SignedData: Chữ ký số cho messages
- ✅ EnvelopedData: Mã hóa messages
- ✅ DigestedData: Message digests
- ✅ EncryptedData: Encrypted messages
- ✅ AuthenticatedData: MAC'd messages

**Ứng dụng:**
- Email signing (S/MIME)
- Document signing
- Code signing

---

### 7️⃣ **OPENPGP**

**Vị trí:** `pg/src/main/java/org/bouncycastle/openpgp/`

**Chức năng:**
- ✅ Key generation (RSA, DSA, ECDSA, EdDSA)
- ✅ Message encryption
- ✅ Message signing
- ✅ Key ring management
- ✅ ASCII armor encoding

**Use cases:**
- Email encryption (GPG)
- File encryption
- Software distribution signing

---

### 8️⃣ **S/MIME (SECURE EMAIL)**

**Vị trí:** `mail/src/main/java/org/bouncycastle/mail/smime/`

**Chức năng:**
- ✅ Sign emails
- ✅ Encrypt emails
- ✅ Verify email signatures
- ✅ Decrypt emails
- ✅ Certificate-based encryption

---

### 9️⃣ **TIME STAMPING (TSP)**

**Vị trí:** `pkix/src/main/java/org/bouncycastle/tsp/`

**Chức năng:**
- ✅ Generate time stamp requests
- ✅ Create time stamp tokens
- ✅ Validate time stamps
- ✅ RFC 3161 compliant

---

### 🔟 **OCSP (ONLINE CERTIFICATE STATUS PROTOCOL)**

**Vị trí:** `pkix/src/main/java/org/bouncycastle/ocsp/`

**Chức năng:**
- ✅ Create OCSP requests
- ✅ Generate OCSP responses
- ✅ Validate certificate status
- ✅ Real-time revocation checking

---

## 🔬 NGHIỆP VỤ CHI TIẾT - USE CASES

### Use Case 1: Ký và xác thực file với ECDSA

```java
// 1. Tạo key pair
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1"); // P-256
keyGen.initialize(ecSpec, new SecureRandom());
KeyPair keyPair = keyGen.generateKeyPair();

// 2. Đọc file cần ký
byte[] fileData = Files.readAllBytes(Paths.get("document.pdf"));

// 3. Tạo chữ ký
Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
signer.initSign(keyPair.getPrivate());
signer.update(fileData);
byte[] signature = signer.sign();

// 4. Lưu chữ ký
Files.write(Paths.get("document.pdf.sig"), signature);

// 5. Xác thực chữ ký
Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
verifier.initVerify(keyPair.getPublic());
verifier.update(fileData);
boolean isValid = verifier.verify(signature);
```

### Use Case 2: Tạo chứng chỉ X.509 tự ký

```java
// 1. Generate key pair
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
keyGen.initialize(new ECGenParameterSpec("secp256r1"));
KeyPair keyPair = keyGen.generateKeyPair();

// 2. Chuẩn bị thông tin certificate
X500Name issuer = new X500Name("CN=My CA, O=My Org, C=VN");
BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
Date notBefore = new Date();
Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);

SubjectPublicKeyInfo publicKeyInfo = 
    SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

// 3. Build certificate
X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
    issuer, serial, notBefore, notAfter, issuer, publicKeyInfo
);

// 4. Thêm extensions
certBuilder.addExtension(
    Extension.basicConstraints, true, new BasicConstraints(true)
);
certBuilder.addExtension(
    Extension.keyUsage, true, 
    new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign)
);

// 5. Ký certificate
ContentSigner signer = 
    new JcaContentSignerBuilder("SHA256withECDSA")
        .setProvider("BC")
        .build(keyPair.getPrivate());

X509CertificateHolder certHolder = certBuilder.build(signer);

// 6. Convert to X509Certificate
X509Certificate cert = 
    new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(certHolder);
```

### Use Case 3: Mã hóa và giải mã với AES

```java
// 1. Generate key
KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
keyGen.init(256);
SecretKey secretKey = keyGen.generateKey();

// 2. Mã hóa
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
cipher.init(Cipher.ENCRYPT_MODE, secretKey);
byte[] iv = cipher.getIV();
byte[] ciphertext = cipher.doFinal(plaintext);

// 3. Giải mã
cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
byte[] decrypted = cipher.doFinal(ciphertext);
```

### Use Case 4: TLS/SSL Connection với ECDSA

```java
// Server side
TlsServerProtocol server = new TlsServerProtocol(...);
server.accept(new DefaultTlsServer() {
    @Override
    protected TlsCredentials getECDSASignerCredentials() {
        // Load ECDSA certificate and private key
        return new DefaultTlsSignerCredentials(...);
    }
    
    @Override
    protected int[] getSupportedCipherSuites() {
        return new int[] {
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
        };
    }
});
```

### Use Case 5: S/MIME Email Signing

```java
// 1. Load certificate and private key
X509Certificate cert = ...;
PrivateKey privateKey = ...;

// 2. Create message
MimeBodyPart bodyPart = new MimeBodyPart();
bodyPart.setText("Secure email content");

// 3. Sign message
SMIMESignedGenerator gen = new SMIMESignedGenerator();
gen.addSignerInfoGenerator(
    new JcaSimpleSignerInfoGeneratorBuilder()
        .setProvider("BC")
        .build("SHA256withECDSA", privateKey, cert)
);

MimeMultipart signedData = gen.generate(bodyPart);
```

---

## 🔐 ECDSA - TRỌNG TÂM DỰ ÁN

### Đường cong Elliptic được hỗ trợ:

1. **NIST Curves (Standard)**
   - secp256r1 (P-256) - 256-bit
   - secp384r1 (P-384) - 384-bit
   - secp521r1 (P-521) - 521-bit

2. **Brainpool Curves (European)**
   - brainpoolP256r1
   - brainpoolP384r1
   - brainpoolP512r1

3. **Koblitz Curves**
   - secp256k1 (Bitcoin uses this!)

4. **ANSI X9.62 Curves**
   - prime192v1, prime256v1

### ECDSA Variants:

1. **Standard ECDSA**
   - Random nonce (k)
   - Signature: (r, s)

2. **Deterministic ECDSA (RFC 6979)**
   - Deterministic nonce generation
   - Không cần RNG tốt
   - An toàn hơn với bad RNG

3. **Plain ECDSA (BSI TR-03111)**
   - German standard
   - Raw signature format

---

## 📊 PERFORMANCE & BENCHMARKING

**Module:** `core/` có JMH benchmarks

**Công cụ:**
- JMH (Java Microbenchmark Harness)
- Performance tests cho:
  - ECDSA signing/verification
  - AES encryption/decryption
  - Hash functions
  - Key generation

**Chạy benchmarks:**
```bash
./gradlew :core:run
```

---

## 🧪 TESTING

### Test Structure:
```
test/
├── src/test/java/org/bouncycastle/
│   ├── crypto/test/
│   │   ├── ECTest.java           # EC math tests
│   │   ├── ECDSATest.java        # ECDSA algorithm tests
│   │   └── AESTest.java          # AES tests
│   ├── jce/provider/test/
│   │   ├── ECDSATest.java        # JCE provider tests
│   │   └── CertTest.java         # Certificate tests
│   └── cms/test/
│       └── CMSSignedDataTest.java
```

### Chạy tests:
```bash
# All tests
./gradlew test

# Test trên Java 11
./gradlew test11

# Test trên Java 17
./gradlew test17

# Test trên Java 21
./gradlew test21
```

---

## 🔄 BUILD & DEPLOYMENT FLOW

```
┌─────────────────────────────────────────────────────────┐
│                    BUILD FLOW                            │
└─────────────────────────────────────────────────────────┘

1. Environment Setup
   ├─ Set BC_JDK8, BC_JDK11, BC_JDK17, BC_JDK21
   └─ Verify Java versions

2. Clean & Compile
   ├─ gradlew clean
   ├─ Compile core module
   ├─ Compile prov module
   ├─ Compile specialized modules
   └─ Generate multi-release JARs

3. Quality Checks
   ├─ Checkstyle (code style)
   ├─ SpotBugs (bug detection)
   ├─ ErrorProne (static analysis)
   └─ NoHTTP check

4. Testing
   ├─ Unit tests (JUnit)
   ├─ Integration tests
   ├─ Test on Java 8
   ├─ Test on Java 11
   ├─ Test on Java 17
   └─ Test on Java 21

5. Package
   ├─ Create JARs
   ├─ Multi-release JAR structure:
   │  ├─ META-INF/versions/9/
   │  ├─ META-INF/versions/11/
   │  ├─ META-INF/versions/15/
   │  └─ META-INF/versions/21/
   └─ Generate documentation

6. Distribution
   ├─ bcprov-jdk18on.jar  (Provider)
   ├─ bcpkix-jdk18on.jar  (PKIX/CMS)
   ├─ bctls-jdk18on.jar   (TLS)
   ├─ bcpg-jdk18on.jar    (OpenPGP)
   └─ bcmail-jdk18on.jar  (S/MIME)
```

---

## 🚀 CÁCH SỬ DỤNG DỰ ÁN

### 1. Build từ source:
```bash
# Set environment variables (Windows PowerShell)
$env:BC_JDK8="C:\Program Files\Java\jdk1.8.0"
$env:BC_JDK11="C:\Program Files\Java\jdk-11"
$env:BC_JDK17="C:\Program Files\Java\jdk-17"
$env:BC_JDK21="C:\Program Files\Java\jdk-21"

# Build
.\gradlew clean build
```

### 2. Sử dụng làm thư viện:

**Maven:**
```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.77</version>
</dependency>
```

**Gradle:**
```gradle
implementation 'org.bouncycastle:bcprov-jdk18on:1.77'
implementation 'org.bouncycastle:bcpkix-jdk18on:1.77'
```

### 3. Đăng ký Provider:
```java
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

// Thêm BC provider vào runtime
Security.addProvider(new BouncyCastleProvider());

// Hoặc set làm provider mặc định
Security.insertProviderAt(new BouncyCastleProvider(), 1);
```

---

## 📈 ROADMAP & FEATURES

### Hiện tại hỗ trợ:
✅ ECDSA (Full implementation)  
✅ RSA, DSA  
✅ AES, DES, 3DES, ChaCha20  
✅ SHA-2, SHA-3, BLAKE2  
✅ X.509 certificates  
✅ TLS 1.3  
✅ Post-Quantum Crypto (ML-DSA, Falcon)  
✅ Composite signatures  

### Quantum-Resistant Algorithms:
✅ ML-DSA (formerly Dilithium)  
✅ Falcon  
✅ SPHINCS+  
✅ Kyber (KEM)  
✅ Composite signatures (hybrid classical + PQC)  

---

## 🎓 TÀI NGUYÊN HỌC TẬP

### Documentation:
- `docs/index.html` - API documentation
- `docs/specifications.html` - Crypto specifications
- `README.md` - Getting started guide

### Examples:
- `test/src/test/java/` - Comprehensive test examples
- Look for `*Test.java` files for usage examples

### External Resources:
- [Bouncy Castle Official](https://www.bouncycastle.org)
- [ECDSA RFC 6979](https://tools.ietf.org/html/rfc6979)
- [X.509 PKI](https://tools.ietf.org/html/rfc5280)
- [TLS 1.3 RFC 8446](https://tools.ietf.org/html/rfc8446)

---

## 🔒 BẢO MẬT

### Security Features:
- ✅ Constant-time operations (timing attack resistance)
- ✅ Side-channel attack mitigations
- ✅ Secure random number generation
- ✅ Memory zeroization
- ✅ FIPS compliance support (separate version)

### Reporting Security Issues:
- Email: security@bouncycastle.org
- See `SECURITY.md` for details

---

## 📜 KẾT LUẬN

**Bouncy Castle (NCKK-ECDSA)** là một thư viện mật mã học **rất hoàn chỉnh và chuyên nghiệp** cho Java với:

✨ **Điểm mạnh:**
- Coverage đầy đủ các thuật toán mật mã
- ECDSA implementation xuất sắc
- Multi-version Java support
- Production-ready quality
- Active maintenance
- Quantum-resistant crypto support

🎯 **Phù hợp cho:**
- Ứng dụng banking/finance
- Blockchain applications
- Secure communication systems
- Digital signature systems
- PKI infrastructure
- IoT security
- Government applications

🔥 **Đặc biệt quan trọng:**
- **ECDSA** là trọng tâm (trong tên dự án)
- Hỗ trợ đầy đủ elliptic curves
- Performance cao
- Security tốt
- Compliance với standards quốc tế

---

📝 **Tài liệu này được tạo để phân tích dự án NCKK-ECDSA**  
📅 **Ngày:** 2025-12-07  
🔖 **Phiên bản:** 1.0
