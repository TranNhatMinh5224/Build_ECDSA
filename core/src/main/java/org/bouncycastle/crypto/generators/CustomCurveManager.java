package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;

public class CustomCurveManager
{
    /**
     * Kết quả trả về từ SageMath
     */
    public static class SageMathResult
    {
        public BigInteger n;
        public BigInteger h;
        public BigInteger Gx;
        public BigInteger Gy;
        public BigInteger Ncurve;
        public boolean nIsPrime;
        public boolean hIsOne;
        public String error;

        public boolean hasError()
        {
            return error != null && !error.trim().isEmpty() && !"none".equalsIgnoreCase(error.trim());
        }
    }

    /**
     * PHASE A: Sinh Raw Curve (p, a, b, seed).
     * Kết quả chưa dùng được ngay vì thiếu n, h, G.
     */
    public static CustomECGenerator.RawCurveData generateRawCurveSteps(int L, int N)
    {
        SecureRandom random = new SecureRandom();
        System.out.println("--- STEP 1: Generating Safe Prime & Raw Curve ---");

        // 1. Sinh Safe Prime p
        BigInteger[] pq = CustomECGenerator.generateSafePrime(L, N, random, 80);
        BigInteger p = pq[0];

        // 2. Sinh a, b từ seed (ANSI X9.62, a = -3 mod p)
        CustomECGenerator.RawCurveData raw = CustomECGenerator.generateRawCurve(p, random);

        System.out.println("Raw Curve Generated Successfully!");
        printHex("p", raw.p);
        printHex("a", raw.a);
        printHex("b", raw.b);
        System.out.println("Seed: " + Hex.toHexString(raw.seedE));
        System.out.println("--> NEXT ACTION: Use external tool (SEA / Sage) to find order 'n' and G.");

        return raw;
    }

    /**
     * PHASE B: Gọi SageMath script để tính order và generator
     * FIX: truyền a.mod(p), b.mod(p) để tránh a = -3 gây lỗi parse.
     * FIX: timeout 300s để tránh treo.
     * FIX: kiểm tra script path tồn tại.
     */
    public static SageMathResult callSageMathForOrder(
            BigInteger p, BigInteger a, BigInteger b, String sageScriptPath)
    {

        SageMathResult result = new SageMathResult();

        try
        {
            // Default path
            if (sageScriptPath == null)
            {
                sageScriptPath = "test/scripts/compute_order_complete.py";
            }

            File scriptFile = new File(sageScriptPath);
            if (!scriptFile.exists())
            {
                result.error = "Sage script not found: " + sageScriptPath;
                return result;
            }

            // Đưa a, b về mod p trước khi truyền
            BigInteger aMod = a.mod(p);
            BigInteger bMod = b.mod(p);

            // WSL path for the script (SageMath runs inside Ubuntu-20.04)
            String wslScript = "/mnt/d/Build_ECDSA/test/scripts/compute_order_complete.py";

            String cmd = String.format(
                "sage %s %s %s %s",
                wslScript,
                p.toString(16),
                aMod.toString(16),
                bMod.toString(16)
            );

            // Call Sage via WSL (Ubuntu-20.04)
            ProcessBuilder pb = new ProcessBuilder(
                "wsl",
                "-d", "Ubuntu-20.04",
                "bash", "-lc",
                cmd
            );
            // pb.directory(new File(".")); // optional: set working dir

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Đọc output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append("\n");
                }
            }

            // Timeout 300 giây
            boolean ok = process.waitFor(3600, TimeUnit.SECONDS);
            if (!ok)
            {
                process.destroyForcibly();
                result.error = "SageMath timeout";
                return result;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0)
            {
                result.error = "SageMath process exited with code: " + exitCode;
                return result;
            }

            // Parse JSON output (một dòng) bằng regex
            String jsonStr = output.toString().trim();
            result = parseSageMathJSON(jsonStr);

        }
        catch (Exception e)
        {
            result.error = "Error calling SageMath: " + e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Helper: lấy value theo key từ JSON một dòng.
     */
    private static String pickString(String json, String key)
    {
        Matcher m = Pattern.compile(
            "\"" + Pattern.quote(key) + "\"\\s*:\\s*(null|\"(.*?)\"|[^,}\\s]+)"
        ).matcher(json);
        if (!m.find())
        {
            return null;
        }
        String raw = m.group(1);
        if ("null".equals(raw))
        {
            return null;
        }
        if (raw.startsWith("\""))
        {
            return m.group(2); // value inside quotes
        }
        return raw; // bare value
    }

    /**
     * Parse JSON output từ SageMath (one-line).
     */
    private static SageMathResult parseSageMathJSON(String jsonStr)
    {
        SageMathResult result = new SageMathResult();
        try
        {
            jsonStr = jsonStr.replaceAll("\\s+", " ").trim();

            String nStr      = pickString(jsonStr, "n");
            String hStr      = pickString(jsonStr, "h");
            String gxStr     = pickString(jsonStr, "Gx");
            String gyStr     = pickString(jsonStr, "Gy");
            String ncStr     = pickString(jsonStr, "Ncurve");
            String nPrimeStr = pickString(jsonStr, "n_is_prime");
            String hOneStr   = pickString(jsonStr, "h_is_one");
            String errStr    = pickString(jsonStr, "error");

            if (nStr != null)      result.n = new BigInteger(nStr, 16);
            if (hStr != null)      result.h = new BigInteger(hStr);
            if (gxStr != null)     result.Gx = new BigInteger(gxStr, 16);
            if (gyStr != null)     result.Gy = new BigInteger(gyStr, 16);
            if (ncStr != null)     result.Ncurve = new BigInteger(ncStr, 16);
            if (nPrimeStr != null) result.nIsPrime = Boolean.parseBoolean(nPrimeStr);
            if (hOneStr != null)   result.hIsOne = Boolean.parseBoolean(hOneStr);
            result.error = errStr; // có thể null

        }
        catch (Exception e)
        {
            result.error = "Error parsing JSON: " + e.getMessage();
        }
        return result;
    }

    /**
     * PHASE C: Finalize curve với G từ SageMath (strict)
     * Checklist: verify seed, n prime, h = 1, G valid, n*G = O.
     */
    public static ECDomainParameters finalizeCurveStrictWithG(
            CustomECGenerator.RawCurveData raw,
            BigInteger n,
            BigInteger h,
            BigInteger Gx,
            BigInteger Gy)
    {

        System.out.println("\n--- STEP 2: Finalizing & Validating Curve (with G from SageMath) ---");

        if (n == null || h == null || Gx == null || Gy == null)
        {
            throw new IllegalArgumentException("REJECT: Missing parameters from SageMath.");
        }

        BigInteger p = raw.p;
        BigInteger a = raw.a;
        BigInteger b = raw.b;

        // 1) Verify seed -> (a,b)
        if (!CustomECGenerator.verifyRandomCurveFp(p, raw.seedE, a, b))
        {
            throw new SecurityException("REJECT: Curve parameters a,b do not match seed!");
        }

        ECCurve.Fp curve = new ECCurve.Fp(p, a, b);

        // 2) Tạo G
        ECPoint G = curve.createPoint(Gx, Gy);

        if (G.isInfinity())
        {
            throw new IllegalStateException("REJECT: G from SageMath is infinity!");
        }
        if (!G.isValid())
        {
            throw new IllegalStateException("REJECT: G from SageMath is not on curve!");
        }

        // 3) n prime
        if (!n.isProbablePrime(40))
        {
            throw new IllegalArgumentException("REJECT: Provided order n is NOT prime.");
        }

        // 4) h == 1 (strict requirement)
        if (!h.equals(BigInteger.ONE))
        {
            throw new IllegalArgumentException("REJECT: Cofactor h != 1. h = " + h);
        }

        // 5) n * G = O
        if (!G.multiply(n).isInfinity())
        {
            throw new IllegalArgumentException("REJECT: Invalid order! n * G != O.");
        }

        System.out.println("All checks passed!");
        System.out.println("Curve Validated and Registered Successfully!");
        printHex("Gx", G.getAffineXCoord().toBigInteger());
        printHex("Gy", G.getAffineYCoord().toBigInteger());
        printHex("n ", n);
        System.out.println("h : " + h.toString());

        return new ECDomainParameters(curve, G, n, h, raw.seedE);
    }

    /**
     * PRODUCTION WORKFLOW: Generate & Register curve tự động (chỉ nhận h=1, n prime)
     * maxAttempts nên đặt cao (ví dụ 200) vì Ncurve prime hiếm.
     * Tự động check file đã lưu trước, nếu có thì load luôn, không cần sinh lại.
     */
    public static ECDomainParameters generateAndRegisterCurve(
            String curveName, int L, int N, int maxAttempts, String sageScriptPath)
    {

        // Đường dẫn file lưu curve - tự động tìm từ nhiều vị trí
        String curveFile = null;
        String[] possiblePaths = {
            "curves/" + curveName + ".json",  // relative to current dir
            System.getProperty("user.dir") + "/curves/" + curveName + ".json",  // from working dir
            "D:/Build_ECDSA/curves/" + curveName + ".json"  // absolute fallback
        };

        for (String path : possiblePaths)
        {
            File testFile = new File(path);
            if (testFile.exists())
            {
                curveFile = path;
                break;
            }
        }

        if (curveFile == null)
        {
            curveFile = "curves/" + curveName + ".json";  // default fallback
        }
        
        // BƯỚC 1: Kiểm tra file đã tồn tại chưa
        try
        {
            ECDomainParameters loaded = loadCurveFromFile(curveFile);
            if (loaded != null)
            {
                System.out.println("========================================");
                System.out.println("Using existing curve from file: " + curveFile);
                System.out.println("========================================");
                return loaded;
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not load curve from file: " + e.getMessage());
            System.out.println("Will generate new curve...");
        }

        // BƯỚC 2: Nếu chưa có file, sinh curve mới
        System.out.println("========================================");
        System.out.println("PRODUCTION WORKFLOW: Generate & Register Curve");
        System.out.println("========================================");
        System.out.println("Curve Name: " + curveName);
        System.out.println("L (p bits): " + L);
        System.out.println("N (q bits): " + N);
        System.out.println("Max Attempts: " + maxAttempts);
        System.out.println("Requirement: h = 1 (Ncurve must be prime)");
        System.out.println();

        SecureRandom random = new SecureRandom();

        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            System.out.println("--- Attempt " + attempt + "/" + maxAttempts + " ---");

            try
            {
                // PHASE A
                System.out.println("PHASE A: Generating raw curve...");
                BigInteger[] pq = CustomECGenerator.generateSafePrime(L, N, random, 80);
                BigInteger p = pq[0];
                CustomECGenerator.RawCurveData raw = CustomECGenerator.generateRawCurve(p, random);

                System.out.println("  Raw curve generated:");
                printHex("  p", raw.p);
                printHex("  a", raw.a);
                printHex("  b", raw.b);

                // PHASE B
                System.out.println("\nPHASE B: Calling SageMath to compute order...");
                SageMathResult sageResult = callSageMathForOrder(raw.p, raw.a, raw.b, sageScriptPath);

                if (sageResult.hasError())
                {
                    System.out.println("  ERROR: " + sageResult.error);
                    if (sageResult.error != null && sageResult.error.contains("h != 1"))
                    {
                        System.out.println("  → Ncurve is not prime, trying new seed...");
                    }
                    continue; // Thử seed khác
                }

                // Guard: đảm bảo không null trước khi in
                if (sageResult.n == null || sageResult.h == null
                    || sageResult.Gx == null || sageResult.Gy == null)
                {
                    System.out.println("  ERROR: Missing fields from Sage output.");
                    continue; // Thử seed khác
                }

                System.out.println("  SageMath result:");
                printHex("  n", sageResult.n);
                System.out.println("  h: " + sageResult.h);
                printHex("  Gx", sageResult.Gx);
                printHex("  Gy", sageResult.Gy);
                System.out.println("  n is prime: " + sageResult.nIsPrime);
                System.out.println("  h is one: " + sageResult.hIsOne);

                // Chỉ nhận h=1 và n prime
                if (!sageResult.hIsOne)
                {
                    System.out.println("  REJECT: h != 1, trying new seed...");
                    continue;
                }
                if (!sageResult.nIsPrime)
                {
                    System.out.println("  REJECT: n is not prime, trying new seed...");
                    continue;
                }

                // PHASE C
                System.out.println("\nPHASE C: Finalizing curve...");
                ECDomainParameters params = finalizeCurveStrictWithG(
                    raw, sageResult.n, sageResult.h, sageResult.Gx, sageResult.Gy);

                // BƯỚC 3: Lưu curve vào file
                try
                {
                    saveCurveToFile(params, curveName, curveFile);
                }
                catch (IOException e)
                {
                    System.err.println("Warning: Could not save curve to file: " + e.getMessage());
                }

                System.out.println("\nSUCCESS! Curve registered: " + curveName);
                System.out.println("========================================");

                return params;

            }
            catch (Exception e)
            {
                System.out.println("  ERROR in attempt " + attempt + ": " + e.getMessage());
                e.printStackTrace();
                continue; // Thử lại
            }
        }

        System.out.println("\nFAILED: Could not generate curve with h=1 after " + maxAttempts + " attempts");
        System.out.println("========================================");
        return null;
    }

    // Helper in Hex chuẩn
    private static void printHex(String label, BigInteger val)
    {
        System.out.println(label + ": " + Hex.toHexString(BigIntegers.asUnsignedByteArray(val)));
    }

    /**
     * Lưu curve parameters vào file JSON
     */
    public static void saveCurveToFile(ECDomainParameters params, String curveName, String filePath) throws IOException
    {
        ECCurve.Fp curve = (ECCurve.Fp) params.getCurve();
        BigInteger p = curve.getQ();  // modulus
        BigInteger a = curve.getA().toBigInteger();
        BigInteger b = curve.getB().toBigInteger();
        BigInteger Gx = params.getG().getAffineXCoord().toBigInteger();
        BigInteger Gy = params.getG().getAffineYCoord().toBigInteger();
        BigInteger n = params.getN();
        BigInteger h = params.getH();
        byte[] seed = params.getSeed();
        
        // Tạo JSON string
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"name\": \"").append(curveName).append("\",\n");
        json.append("  \"p\": \"").append(p.toString(16)).append("\",\n");
        json.append("  \"a\": \"").append(a.toString(16)).append("\",\n");
        json.append("  \"b\": \"").append(b.toString(16)).append("\",\n");
        json.append("  \"Gx\": \"").append(Gx.toString(16)).append("\",\n");
        json.append("  \"Gy\": \"").append(Gy.toString(16)).append("\",\n");
        json.append("  \"n\": \"").append(n.toString(16)).append("\",\n");
        json.append("  \"h\": \"").append(h.toString()).append("\"");
        if (seed != null && seed.length > 0)
        {
            json.append(",\n  \"seed\": \"").append(Hex.toHexString(seed)).append("\"");
        }
        json.append("\n}");
        
        // Tạo thư mục nếu chưa có
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists())
        {
            parentDir.mkdirs();
        }
        
        // Ghi file
        Files.write(Paths.get(filePath), json.toString().getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        System.out.println("Curve saved to: " + filePath);
    }

    /**
     * Load curve parameters từ file JSON
     */
    public static ECDomainParameters loadCurveFromFile(String filePath) throws IOException
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            return null;
        }
        
        // Đọc file JSON
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        
        // Parse JSON
        String pStr = extractJsonValue(content, "p");
        String aStr = extractJsonValue(content, "a");
        String bStr = extractJsonValue(content, "b");
        String GxStr = extractJsonValue(content, "Gx");
        String GyStr = extractJsonValue(content, "Gy");
        String nStr = extractJsonValue(content, "n");
        String hStr = extractJsonValue(content, "h");
        String seedStr = extractJsonValue(content, "seed");
        
        if (pStr == null || aStr == null || bStr == null || GxStr == null || 
            GyStr == null || nStr == null || hStr == null)
        {
            throw new IOException("Invalid curve file: missing required fields");
        }
        
        BigInteger p = new BigInteger(pStr, 16);
        BigInteger a = new BigInteger(aStr, 16);
        BigInteger b = new BigInteger(bStr, 16);
        BigInteger Gx = new BigInteger(GxStr, 16);
        BigInteger Gy = new BigInteger(GyStr, 16);
        BigInteger n = new BigInteger(nStr, 16);
        BigInteger h = new BigInteger(hStr);
        byte[] seed = seedStr != null ? Hex.decode(seedStr) : null;
        
        // Tạo curve và domain parameters
        ECCurve.Fp curve = new ECCurve.Fp(p, a, b);
        ECPoint G = curve.createPoint(Gx, Gy);
        
        // Verify
        if (!G.isValid())
        {
            throw new IOException("Invalid curve file: G is not on curve");
        }
        if (!G.multiply(n).isInfinity())
        {
            throw new IOException("Invalid curve file: n*G != O");
        }
        
        System.out.println("Curve loaded from: " + filePath);
        return new ECDomainParameters(curve, G, n, h, seed);
    }

    /**
     * Helper: Extract value từ JSON string
     */
    private static String extractJsonValue(String json, String key)
    {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }
}
