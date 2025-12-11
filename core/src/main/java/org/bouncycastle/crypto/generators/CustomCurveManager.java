package org.bouncycastle.crypto.generators;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Hashtable;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

/**
 * Hệ thống quản lý Custom Curve:
 * 1. Sinh đường cong ngẫu nhiên
 * 2. Verify và lưu lại
 * 3. Đăng ký như named curve
 * 4. Sử dụng lại các lần sau
 */
public class CustomCurveManager
{
    private static final Hashtable<String, ECParameterSpec> registeredCurves = new Hashtable<>();
    private static final String CURVE_DIR = "custom_curves/";
    
    /**
     * Bước 1: Sinh đường cong ngẫu nhiên và verify
     * 
     * @param curveName tên đường cong (ví dụ: "MyCustomCurve-256")
     * @param p số nguyên tố (từ thuật toán 1)
     * @param a hệ số a (từ thuật toán 2)
     * @param b hệ số b (từ thuật toán 2)
     * @param random nguồn random
     * @return true nếu verify thành công và đã lưu
     */
    public static boolean generateAndRegisterCurve(
        String curveName,
        BigInteger p,
        BigInteger a,
        BigInteger b,
        SecureRandom random)
    {
        try {
            // 1. Sinh domain parameters
            System.out.println("Đang sinh đường cong: " + curveName + "...");
            ECDomainParameters domainParams = CustomECGenerator.generateDomainParameters(
                p, a, b, random);
            
            // 2. Verify đầy đủ
            if (!verifyCurve(domainParams)) {
                System.out.println("❌ Verify thất bại!");
                return false;
            }
            
            System.out.println("✅ Verify thành công!");
            
            // 3. Convert sang ECParameterSpec
            ECParameterSpec spec = new ECParameterSpec(
                domainParams.getCurve(),
                domainParams.getG(),
                domainParams.getN(),
                domainParams.getH(),
                domainParams.getSeed()
            );
            
            // 4. Lưu vào memory (đăng ký)
            registeredCurves.put(curveName, spec);
            
            // 5. Lưu vào file để dùng lại sau
            saveToFile(curveName, domainParams);
            
            System.out.println("✅ Đã lưu và đăng ký đường cong: " + curveName);
            printCurveInfo(curveName, domainParams);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi sinh đường cong: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Bước 2: Verify đường cong đầy đủ
     */
    private static boolean verifyCurve(ECDomainParameters params)
    {
        try {
            // 1. Verify G không phải điểm vô cực
            if (params.getG().isInfinity()) {
                System.out.println("  ❌ G là điểm vô cực");
                return false;
            }
            
            // 2. Verify G nằm trên đường cong
            if (!params.getG().isValid()) {
                System.out.println("  ❌ G không nằm trên đường cong");
                return false;
            }
            
            // 3. Verify n * G = O
            ECPoint verify = params.getG().multiply(params.getN());
            if (!verify.isInfinity()) {
                System.out.println("  ❌ n * G ≠ O");
                return false;
            }
            
            // 4. Verify không có order nhỏ hơn
            if (!hasCorrectOrder(params.getG(), params.getN())) {
                System.out.println("  ❌ G có order nhỏ hơn n");
                return false;
            }
            
            // 5. Test sinh key pair
            ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
            ECKeyGenerationParameters keyGenParams = 
                new ECKeyGenerationParameters(params, new SecureRandom());
            keyGen.init(keyGenParams);
            
            AsymmetricCipherKeyPair testPair = keyGen.generateKeyPair();
            if (testPair == null) {
                System.out.println("  ❌ Không thể sinh key pair");
                return false;
            }
            
            // 6. Test ECDSA signing
            if (!testECDSASigning(params)) {
                System.out.println("  ❌ Test ECDSA signing thất bại");
                return false;
            }
            
            System.out.println("  ✅ Tất cả verify đều pass!");
            return true;
            
        } catch (Exception e) {
            System.out.println("  ❌ Lỗi verify: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra điểm có order chính xác
     */
    private static boolean hasCorrectOrder(ECPoint point, BigInteger targetOrder)
    {
        // Verify: targetOrder * point = O
        ECPoint verify = point.multiply(targetOrder);
        if (!verify.isInfinity()) {
            return false;
        }
        
        // Kiểm tra các ước số nhỏ
        BigInteger[] smallPrimes = {
            BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(5),
            BigInteger.valueOf(7), BigInteger.valueOf(11), BigInteger.valueOf(13)
        };
        
        for (BigInteger prime : smallPrimes) {
            if (targetOrder.mod(prime).equals(BigInteger.ZERO)) {
                BigInteger subOrder = targetOrder.divide(prime);
                ECPoint test = point.multiply(subOrder);
                if (test.isInfinity()) {
                    return false; // Có order nhỏ hơn
                }
            }
        }
        
        return true;
    }
    
    /**
     * Test ECDSA signing với đường cong
     */
    private static boolean testECDSASigning(ECDomainParameters params)
    {
        try {
            // Sinh key pair
            ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
            ECKeyGenerationParameters keyGenParams = 
                new ECKeyGenerationParameters(params, new SecureRandom());
            keyGen.init(keyGenParams);
            
            AsymmetricCipherKeyPair pair = keyGen.generateKeyPair();
            
            // Test signing (dùng lightweight API)
            byte[] message = "Test message".getBytes();
            ECDSASigner signer = new ECDSASigner();
            signer.init(true, pair.getPrivate());
            
            BigInteger[] signature = signer.generateSignature(message);
            
            // Test verification
            signer.init(false, pair.getPublic());
            boolean verified = signer.verifySignature(message, signature[0], signature[1]);
            
            return verified;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Bước 3: Lưu vào file
     */
    private static void saveToFile(String curveName, ECDomainParameters params)
    {
        try {
            // Tạo thư mục nếu chưa có
            File dir = new File(CURVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Lưu vào file
            File file = new File(CURVE_DIR + curveName + ".params");
            try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file))) {
                
                // Lưu các tham số
                CurveParams curveParams = new CurveParams();
                curveParams.p = params.getCurve().getQ().toByteArray();
                curveParams.a = params.getCurve().getA().toBigInteger().toByteArray();
                curveParams.b = params.getCurve().getB().toBigInteger().toByteArray();
                curveParams.Gx = params.getG().getAffineXCoord().toBigInteger().toByteArray();
                curveParams.Gy = params.getG().getAffineYCoord().toBigInteger().toByteArray();
                curveParams.n = params.getN().toByteArray();
                curveParams.h = params.getH().toByteArray();
                curveParams.seed = params.getSeed();
                
                oos.writeObject(curveParams);
            }
            
            System.out.println("  💾 Đã lưu vào: " + file.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("  ⚠️  Không thể lưu file: " + e.getMessage());
        }
    }
    
    /**
     * Bước 4: Load từ file và đăng ký lại
     */
    public static boolean loadAndRegisterCurve(String curveName)
    {
        try {
            File file = new File(CURVE_DIR + curveName + ".params");
            if (!file.exists()) {
                System.out.println("❌ File không tồn tại: " + file.getAbsolutePath());
                return false;
            }
            
            // Load từ file
            CurveParams curveParams;
            try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
                curveParams = (CurveParams)ois.readObject();
            }
            
            // Tạo lại ECDomainParameters
            ECCurve.Fp curve = new ECCurve.Fp(
                new BigInteger(1, curveParams.p),
                new BigInteger(1, curveParams.a),
                new BigInteger(1, curveParams.b),
                null, null
            );
            
            ECPoint G = curve.createPoint(
                new BigInteger(1, curveParams.Gx),
                new BigInteger(1, curveParams.Gy)
            );
            
            ECDomainParameters domainParams = new ECDomainParameters(
                curve,
                G,
                new BigInteger(1, curveParams.n),
                new BigInteger(1, curveParams.h),
                curveParams.seed
            );
            
            // Verify lại
            if (!verifyCurve(domainParams)) {
                System.out.println("❌ Verify thất bại khi load!");
                return false;
            }
            
            // Convert và đăng ký
            ECParameterSpec spec = new ECParameterSpec(
                domainParams.getCurve(),
                domainParams.getG(),
                domainParams.getN(),
                domainParams.getH(),
                domainParams.getSeed()
            );
            
            registeredCurves.put(curveName, spec);
            
            System.out.println("✅ Đã load và đăng ký: " + curveName);
            printCurveInfo(curveName, domainParams);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi load: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Bước 5: Lấy đường cong đã đăng ký (dùng như named curve)
     */
    public static ECParameterSpec getCurve(String curveName)
    {
        // Thử load từ memory trước
        ECParameterSpec spec = registeredCurves.get(curveName);
        if (spec != null) {
            return spec;
        }
        
        // Nếu không có, thử load từ file
        if (loadAndRegisterCurve(curveName)) {
            return registeredCurves.get(curveName);
        }
        
        return null;
    }
    
    /**
     * Kiểm tra đường cong đã tồn tại chưa
     */
    public static boolean curveExists(String curveName)
    {
        // Kiểm tra trong memory
        if (registeredCurves.containsKey(curveName)) {
            return true;
        }
        
        // Kiểm tra file
        File file = new File(CURVE_DIR + curveName + ".params");
        return file.exists();
    }
    
    /**
     * In thông tin đường cong
     */
    private static void printCurveInfo(String curveName, ECDomainParameters params)
    {
        System.out.println("\n📊 Thông tin đường cong: " + curveName);
        System.out.println("   p (hex): " + Hex.toHexString(params.getCurve().getQ().toByteArray()));
        System.out.println("   a (hex): " + Hex.toHexString(params.getCurve().getA().toBigInteger().toByteArray()));
        System.out.println("   b (hex): " + Hex.toHexString(params.getCurve().getB().toBigInteger().toByteArray()));
        System.out.println("   Gx (hex): " + Hex.toHexString(params.getG().getAffineXCoord().toBigInteger().toByteArray()));
        System.out.println("   Gy (hex): " + Hex.toHexString(params.getG().getAffineYCoord().toBigInteger().toByteArray()));
        System.out.println("   n (hex): " + Hex.toHexString(params.getN().toByteArray()));
        System.out.println("   h: " + params.getH());
        System.out.println();
    }
    
    /**
     * Class để lưu tham số đường cong
     */
    private static class CurveParams implements Serializable
    {
        byte[] p;
        byte[] a;
        byte[] b;
        byte[] Gx;
        byte[] Gy;
        byte[] n;
        byte[] h;
        byte[] seed;
    }
}

