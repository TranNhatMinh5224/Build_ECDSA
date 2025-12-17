# Hướng Dẫn Test Custom Curve trong EJBCA

## Bước 1: Xác định đường dẫn EJBCA

EJBCA thường được cài đặt ở:
- `C:\ejbca\lib`
- `C:\Program Files\EJBCA\lib`
- Hoặc thư mục bạn đã cài đặt

Tìm thư mục chứa các file jar của Bouncy Castle (thường là `bcprov-*.jar`, `bcpkix-*.jar`).

## Bước 2: Chạy script test

```powershell
.\test-ejbca-curve.ps1 -EjbcaLibPath "C:\ejbca\lib"
```

Script sẽ:
1. Backup jars cũ
2. Copy jars mới vào EJBCA
3. Compile và chạy test Java để verify curve
4. Hiển thị kết quả

## Bước 3: Restart EJBCA

Sau khi thay jars, **BẮT BUỘC** phải restart EJBCA:

### Nếu EJBCA chạy như Windows Service:
```powershell
# Tìm service name
Get-Service | Where-Object {$_.DisplayName -like "*EJBCA*"}

# Restart service
Restart-Service -Name "EJBCA"  # Thay tên service cho đúng
```

### Nếu EJBCA chạy trong WildFly/JBoss:
```powershell
# Stop WildFly
.\jboss-cli.bat --connect command=:shutdown

# Start WildFly
.\standalone.bat
```

### Nếu EJBCA chạy trong Docker:
```powershell
docker restart ejbca-container-name
```

## Bước 4: Verify trong EJBCA Admin UI

### 4.1. Đăng nhập EJBCA Admin UI
- Mở browser: `https://localhost:8443/ejbca/adminweb/`
- Đăng nhập với admin account

### 4.2. Kiểm tra Curve có sẵn

**Cách 1: Qua Certificate Profiles**
1. Vào **Certificate Profiles** → **Add Certificate Profile**
2. Chọn **ENDENTITY** profile
3. Trong phần **Key Algorithm**, chọn **ECDSA**
4. Trong dropdown **Named Curve**, tìm **MyCustomCurve-256**
5. Nếu thấy → Curve đã được load thành công!

**Cách 2: Qua Key Generation**
1. Vào **Crypto Tokens** → Chọn token
2. Vào **Generate Keys**
3. Chọn **ECDSA** algorithm
4. Trong dropdown **Named Curve**, tìm **MyCustomCurve-256**

**Cách 3: Qua CLI (ejbca.sh)**
```bash
./ejbca.sh ca listcas
./ejbca.sh keytool -list -keystore keystore.p12 -storepass changeit
```

### 4.3. Tạo Certificate với Custom Curve

1. Vào **Certificate Profiles** → Tạo profile mới với curve `MyCustomCurve-256`
2. Vào **End Entities** → **Add End Entity**
3. Chọn profile vừa tạo
4. Generate certificate
5. Download certificate

### 4.4. Verify OID trong Certificate

**Cách 1: Dùng OpenSSL**
```bash
openssl x509 -in certificate.pem -text -noout | grep -A 5 "ASN1 OID"
```

**Cách 2: Dùng Java**
```java
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.cert.X509CertificateHolder;
import static org.bouncycastle.asn1.custom.CustomCurveObjectIdentifiers.myCustomCurve256;

// Load certificate
X509Certificate cert = ...; // Load từ file
X509CertificateHolder holder = new X509CertificateHolder(cert.getEncoded());
SubjectPublicKeyInfo spki = holder.getSubjectPublicKeyInfo();
X962Parameters params = X962Parameters.getInstance(spki.getAlgorithm().getParameters());

if (params.isNamedCurve()) {
    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params.getParameters();
    if (oid.equals(myCustomCurve256)) {
        System.out.println("Certificate uses custom curve! OID: " + oid.getId());
    }
}
```

**Cách 3: Dùng EJBCA Admin UI**
1. Vào **Search Certificates**
2. Tìm certificate vừa tạo
3. View certificate details
4. Kiểm tra **Public Key Algorithm Parameters** → OID phải là `1.3.6.1.4.1.99999.1`

## Bước 5: Troubleshooting

### Curve không hiện trong dropdown
- **Nguyên nhân**: EJBCA chưa restart hoặc jars chưa được load
- **Giải pháp**: 
  1. Kiểm tra jars đã được copy chưa
  2. Restart EJBCA
  3. Kiểm tra logs: `ejbca.log` hoặc `server.log`

### Lỗi "Curve not found"
- **Nguyên nhân**: CustomNamedCurves chưa được register
- **Giải pháp**: 
  1. Verify jars có chứa `CustomNamedCurves.class`
  2. Chạy `verify-custom-curve.ps1` để kiểm tra

### Lỗi "Provider BC not found"
- **Nguyên nhân**: Bouncy Castle provider chưa được add
- **Giải pháp**: EJBCA tự động load provider từ jars, nếu lỗi thì kiểm tra classpath

### Certificate không dùng custom curve OID
- **Nguyên nhân**: Certificate profile không được config đúng
- **Giải pháp**: 
  1. Tạo lại certificate profile với curve `MyCustomCurve-256`
  2. Regenerate certificate

## Checklist

- [ ] Jars đã được copy vào EJBCA lib directory
- [ ] EJBCA đã được restart
- [ ] Curve `MyCustomCurve-256` hiện trong dropdown
- [ ] Certificate được tạo thành công
- [ ] OID trong certificate là `1.3.6.1.4.1.99999.1`
- [ ] Certificate verification thành công

## Test Scripts

- `replace-bc-jars.ps1` - Thay jars vào EJBCA
- `verify-custom-curve.ps1` - Verify curve trong jar
- `test-ejbca-curve.ps1` - Test curve trong EJBCA context

## Lưu ý

1. **Backup**: Luôn backup jars cũ trước khi thay
2. **Version**: Đảm bảo version BC trong jars tương thích với EJBCA
3. **Restart**: Bắt buộc restart sau khi thay jars
4. **OID**: OID `1.3.6.1.4.1.99999.1` chỉ dùng cho lab/demo. Production cần dùng PEN thật.

