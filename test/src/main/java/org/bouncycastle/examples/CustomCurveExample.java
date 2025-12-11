package org.bouncycastle.examples;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import org.bouncycastle.crypto.generators.CustomCurveManager;
import org.bouncycastle.crypto.generators.CustomECGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

/**
 * Ví dụ: Sinh, verify, lưu và sử dụng lại đường cong
 * 
 * Flow:
 * 1. Kiểm tra đường cong đã tồn tại chưa
 * 2. Nếu chưa, sinh mới (prime, curve, domain params)
 * 3. Verify và lưu vào file
 * 4. Sử dụng như named curve để ký và xác thực
 * 5. Các lần sau: load từ file và dùng lại
 */
public class CustomCurveExample
{
    public static void main(String[] args) throws Exception
    {
        // Đăng ký Bouncy Castle Provider
        Security.addProvider(new BouncyCastleProvider());
        
        String curveName = "MyCustomCurve-256";
        SecureRandom random = new SecureRandom();
        
        System.out.println("==========================================");
        System.out.println("CUSTOM CURVE ECDSA EXAMPLE");
        System.out.println("==========================================\n");
        
        // ============================================
        // BƯỚC 1: KIỂM TRA ĐÃ TỒN TẠI CHƯA
        // ============================================
        if (CustomCurveManager.curveExists(curveName)) {
            System.out.println("✅ Đường cong đã tồn tại, load từ file...\n");
            CustomCurveManager.loadAndRegisterCurve(curveName);
        } else {
            // ============================================
            // BƯỚC 2: SINH ĐƯỜNG CONG MỚI
            // ============================================
            System.out.println("🔄 Sinh đường cong mới...\n");
            
            // Thuật toán 1: Sinh safe prime p và q
            System.out.println("📝 Bước 1: Sinh safe prime...");
            BigInteger[] primes = CustomECGenerator.generateSafePrime(256, 64, random);
            if (primes == null) {
                System.err.println("❌ Không thể sinh safe prime!");
                return;
            }
            BigInteger p = primes[0];
            BigInteger q = primes[1];
            System.out.println("   ✅ p (256 bits): " + p.toString(16));
            System.out.println("   ✅ q (64 bits): " + q.toString(16));
            System.out.println();
            
            // Thuật toán 2: Sinh đường cong ngẫu nhiên
            System.out.println("📝 Bước 2: Sinh đường cong ngẫu nhiên...");
            byte[] seed_E = new byte[20];
            random.nextBytes(seed_E);
            BigInteger[] curveParams = CustomECGenerator.generateRandomCurve(p, seed_E);
            BigInteger a = curveParams[0];
            BigInteger b = curveParams[1];
            System.out.println("   ✅ a: " + a.toString(16));
            System.out.println("   ✅ b: " + b.toString(16));
            System.out.println();
            
            // Thuật toán 3: Sinh domain parameters, verify và lưu
            System.out.println("📝 Bước 3: Sinh domain parameters...");
            boolean success = CustomCurveManager.generateAndRegisterCurve(
                curveName, p, a, b, random);
            
            if (!success) {
                System.err.println("❌ Không thể sinh đường cong!");
                return;
            }
        }
        
        // ============================================
        // BƯỚC 3: SỬ DỤNG NHƯ NAMED CURVE
        // ============================================
        System.out.println("\n🔑 Sử dụng đường cong để sinh key pair...");
        
        // Lấy đường cong đã đăng ký
        ECParameterSpec spec = CustomCurveManager.getCurve(curveName);
        if (spec == null) {
            System.err.println("❌ Không thể load đường cong!");
            return;
        }
        
        // Sinh key pair (giống hệt named curve!)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(spec, random);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        System.out.println("✅ Đã sinh key pair thành công!");
        System.out.println();
        
        // ============================================
        // BƯỚC 4: KÝ VÀ XÁC THỰC
        // ============================================
        System.out.println("✍️  Test ký và xác thực...\n");
        
        byte[] message = "Hello Custom Curve ECDSA!".getBytes();
        
        // Ký
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(message);
        byte[] signature = signer.sign();
        
        System.out.println("✅ Đã ký thành công!");
        System.out.println("   Message: " + new String(message));
        System.out.println("   Signature length: " + signature.length + " bytes");
        System.out.println();
        
        // Xác thực
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(message);
        boolean valid = verifier.verify(signature);
        
        if (valid) {
            System.out.println("✅ Xác thực thành công!");
        } else {
            System.out.println("❌ Xác thực thất bại!");
        }
        System.out.println();
        
        // ============================================
        // BƯỚC 5: DEMO CÁC LẦN SAU - DÙNG LẠI
        // ============================================
        System.out.println("🔄 Demo: Lần sau, chỉ cần load lại...\n");
        
        // Giả lập restart application
        // Chỉ cần:
        ECParameterSpec spec2 = CustomCurveManager.getCurve(curveName);
        // → Tự động load từ file, verify và đăng ký lại!
        
        if (spec2 != null) {
            KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("EC", "BC");
            keyGen2.initialize(spec2, random);
            KeyPair keyPair2 = keyGen2.generateKeyPair();
            
            System.out.println("✅ Dùng lại thành công! (giống hệt named curve)");
            System.out.println("   → Cùng tham số như lần đầu!");
            System.out.println("   → Tốc độ nhanh như named curve!");
        }
        
        System.out.println("\n==========================================");
        System.out.println("HOÀN THÀNH!");
        System.out.println("==========================================");
    }
}

