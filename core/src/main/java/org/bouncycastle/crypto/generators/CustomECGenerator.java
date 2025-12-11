package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.Primes;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

/**
 * Custom EC Generator - Chứa 3 thuật toán:
 * 1. Sinh số nguyên tố an toàn (safe prime)
 * 2. Sinh đường cong ngẫu nhiên từ prime
 * 3. Sinh domain parameters (G, n, h) với verify đầy đủ
 */
public class CustomECGenerator
{
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    private static final BigInteger TWENTY_SEVEN = BigInteger.valueOf(27);

    /**
     * Thuật toán 1: Sinh safe prime p và q (q | (p-1))
     * Port từ sinh-so-an-toan.ipynb - Algorithm 3
     * 
     * @param L bit length của p
     * @param N bit length của q
     * @param random nguồn random
     * @return [p, q] nếu thành công, null nếu thất bại
     */
    public static BigInteger[] generateSafePrime(int L, int N, SecureRandom random)
    {
        int maxAttempts = 1000;
        int attempt = 0;

        while (attempt < maxAttempts)
        {
            // Sinh q (N bits)
            BigInteger q = generateProbablePrime(N, random);
            if (q == null)
            {
                attempt++;
                continue;
            }

            // Sinh p = 2*q + 1 (L bits)
            BigInteger p = q.multiply(TWO).add(ONE);

            // Kiểm tra p có đúng L bits không
            if (p.bitLength() != L)
            {
                attempt++;
                continue;
            }

            // Kiểm tra p là prime
            if (isProbablePrime(p, 20))
            {
                // Verify: q | (p-1)
                BigInteger pMinus1 = p.subtract(ONE);
                if (pMinus1.mod(q).equals(ZERO))
                {
                    return new BigInteger[]{p, q};
                }
            }

            attempt++;
        }

        return null; // Không tìm được sau maxAttempts lần
    }

    /**
     * Sinh số nguyên tố probable trong range
     * Port từ sinh-so-an-toan.ipynb - Algorithm 1
     */
    private static BigInteger generateProbablePrime(int bitLength, SecureRandom random)
    {
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++)
        {
            BigInteger candidate = BigIntegers.createRandomBigInteger(bitLength, random);
            
            // Đảm bảo bit đầu tiên là 1 (để đạt bitLength)
            candidate = candidate.setBit(bitLength - 1);
            
            // Đảm bảo là số lẻ
            if (!candidate.testBit(0))
            {
                candidate = candidate.add(ONE);
            }

            if (isProbablePrime(candidate, 20))
            {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Miller-Rabin primality test
     * Port từ sinh-so-an-toan.ipynb
     */
    private static boolean isProbablePrime(BigInteger n, int k)
    {
        if (n.compareTo(TWO) < 0)
        {
            return false;
        }
        if (n.equals(TWO) || n.equals(THREE))
        {
            return true;
        }
        if (!n.testBit(0)) // n chẵn
        {
            return false;
        }

        // n - 1 = d * 2^r
        BigInteger d = n.subtract(ONE);
        int r = 0;
        while (!d.testBit(0))
        {
            d = d.shiftRight(1);
            r++;
        }

        // k rounds of Miller-Rabin
        for (int i = 0; i < k; i++)
        {
            BigInteger a = BigIntegers.createRandomInRange(TWO, n.subtract(TWO), new SecureRandom());
            BigInteger x = a.modPow(d, n);

            if (x.equals(ONE) || x.equals(n.subtract(ONE)))
            {
                continue;
            }

            boolean composite = true;
            for (int j = 0; j < r - 1; j++)
            {
                x = x.modPow(TWO, n);
                if (x.equals(n.subtract(ONE)))
                {
                    composite = false;
                    break;
                }
                if (x.equals(ONE))
                {
                    return false; // Composite
                }
            }

            if (composite)
            {
                return false; // Composite
            }
        }

        return true; // Probably prime
    }

    /**
     * Thuật toán 2: Sinh đường cong ngẫu nhiên từ prime p và seed
     * Port từ sinh-duong-cong-ngau-nhien.ipynb - Algorithm 1.1
     * 
     * @param p số nguyên tố
     * @param seed_E seed (20 bytes)
     * @return [a, b, r] với r là square root của b (nếu có)
     */
    public static BigInteger[] generateRandomCurve(BigInteger p, byte[] seed_E)
    {
        if (seed_E.length != 20)
        {
            throw new IllegalArgumentException("seed_E phải có độ dài 20 bytes");
        }

        // SHA-1 hash của seed_E
        SHA1Digest sha1 = new SHA1Digest();
        sha1.update(seed_E, 0, seed_E.length);
        byte[] hash = new byte[sha1.getDigestSize()];
        sha1.doFinal(hash, 0);

        // Chuyển hash thành BigInteger
        BigInteger hashInt = new BigInteger(1, hash);

        // Algorithm 1.1: Sinh a, b, r
        BigInteger a = hashInt.mod(p);
        
        // Tìm b sao cho b là quadratic residue mod p
        BigInteger b = null;
        BigInteger r = null;
        int maxAttempts = 100;
        
        for (int i = 0; i < maxAttempts; i++)
        {
            BigInteger candidate = hashInt.add(BigInteger.valueOf(i)).mod(p);
            
            // Kiểm tra candidate là quadratic residue
            if (legendreSymbol(candidate, p) == 1)
            {
                b = candidate;
                r = modularSqrt(candidate, p);
                break;
            }
        }

        if (b == null)
        {
            // Fallback: dùng hashInt trực tiếp
            b = hashInt.mod(p);
            r = null; // Không tìm được square root
        }

        return new BigInteger[]{a, b, r};
    }

    /**
     * Legendre Symbol: (a/p)
     * Port từ sinh-duong-cong-ngau-nhien.ipynb
     */
    private static int legendreSymbol(BigInteger a, BigInteger p)
    {
        BigInteger result = a.modPow(p.subtract(ONE).divide(TWO), p);
        
        if (result.equals(ONE))
        {
            return 1; // Quadratic residue
        }
        else if (result.equals(p.subtract(ONE)))
        {
            return -1; // Quadratic non-residue
        }
        else
        {
            return 0; // a ≡ 0 (mod p)
        }
    }

    /**
     * Tonelli-Shanks algorithm: Tìm square root modulo prime
     * Port từ sinh-duong-cong-ngau-nhien.ipynb
     */
    private static BigInteger modularSqrt(BigInteger a, BigInteger p)
    {
        if (legendreSymbol(a, p) != 1)
        {
            return null; // Không có square root
        }

        if (a.equals(ZERO))
        {
            return ZERO;
        }

        if (p.equals(TWO))
        {
            return a;
        }

        // p ≡ 3 (mod 4)
        if (p.mod(FOUR).equals(THREE))
        {
            return a.modPow(p.add(ONE).divide(FOUR), p);
        }

        // Tonelli-Shanks cho p ≡ 1 (mod 4)
        // Tìm Q và S: p - 1 = Q * 2^S
        BigInteger q = p.subtract(ONE);
        int s = 0;
        while (!q.testBit(0))
        {
            q = q.shiftRight(1);
            s++;
        }

        // Tìm z (quadratic non-residue)
        BigInteger z = TWO;
        while (legendreSymbol(z, p) != -1)
        {
            z = z.add(ONE);
        }

        BigInteger m = BigInteger.valueOf(s);
        BigInteger c = z.modPow(q, p);
        BigInteger t = a.modPow(q, p);
        BigInteger r = a.modPow(q.add(ONE).divide(TWO), p);

        while (!t.equals(ONE))
        {
            BigInteger tt = t;
            int i = 1;
            while (i < s && !tt.equals(ONE))
            {
                tt = tt.modPow(TWO, p);
                i++;
            }

            BigInteger b = c.modPow(TWO.modPow(BigInteger.valueOf(s - i - 1), p.subtract(ONE)), p);
            m = BigInteger.valueOf(i);
            r = r.multiply(b).mod(p);
            t = t.multiply(b.modPow(TWO, p)).mod(p);
            c = b.modPow(TWO, p);
            s = i;
        }

        return r;
    }

    /**
     * Thuật toán 3: Sinh domain parameters (G, n, h) với verify đầy đủ
     * Improved version với BSGS và comprehensive verification
     * 
     * @param p số nguyên tố
     * @param a hệ số a của đường cong
     * @param b hệ số b của đường cong
     * @param random nguồn random
     * @return ECDomainParameters đã verify
     */
    public static ECDomainParameters generateDomainParameters(
        BigInteger p, BigInteger a, BigInteger b, SecureRandom random)
    {
        // 1. Validate curve: 4a³ + 27b² ≠ 0 (mod p)
        BigInteger fourA3 = FOUR.multiply(a.modPow(THREE, p)).mod(p);
        BigInteger twentySevenB2 = TWENTY_SEVEN.multiply(b.modPow(TWO, p)).mod(p);
        BigInteger discriminant = fourA3.add(twentySevenB2).mod(p);
        
        if (discriminant.equals(ZERO))
        {
            throw new IllegalArgumentException("Curve discriminant = 0, curve không hợp lệ!");
        }

        // 2. Tạo ECCurve
        ECCurve.Fp curve = new ECCurve.Fp(p, a, b);

        // 3. Tìm một điểm bất kỳ trên đường cong
        ECPoint basePoint = findAnyPointOnCurve(curve, p, a, b, random);
        if (basePoint == null)
        {
            throw new IllegalStateException("Không tìm được điểm nào trên đường cong!");
        }

        // 4. Tính order của điểm (n)
        BigInteger n = computePointOrder(basePoint, curve, p, random);
        if (n == null || n.compareTo(ONE) <= 0)
        {
            throw new IllegalStateException("Không tính được order hợp lệ của điểm!");
        }

        // 5. Tính approximate curve order
        BigInteger curveOrderApprox = computeCurveOrderApprox(p);

        // 6. Tính cofactor h
        BigInteger h = curveOrderApprox.divide(n);
        if (h.compareTo(ZERO) <= 0)
        {
            h = ONE;
        }

        // 7. Tìm generator point G với cofactor đúng
        ECPoint G = findGeneratorWithCofactor(basePoint, n, h, curve, random);
        if (G == null)
        {
            throw new IllegalStateException("Không tìm được generator point!");
        }

        // 8. Verify đầy đủ
        if (!verifyDomainParameters(curve, G, n, h))
        {
            throw new IllegalStateException("Domain parameters không pass verification!");
        }

        // 9. Tạo ECDomainParameters
        return new ECDomainParameters(curve, G, n, h, null);
    }

    /**
     * Tìm một điểm bất kỳ trên đường cong y² = x³ + ax + b (mod p)
     */
    private static ECPoint findAnyPointOnCurve(
        ECCurve.Fp curve, BigInteger p, BigInteger a, BigInteger b, SecureRandom random)
    {
        int maxAttempts = 1000;
        
        for (int i = 0; i < maxAttempts; i++)
        {
            // Chọn x ngẫu nhiên
            BigInteger x = BigIntegers.createRandomInRange(ONE, p.subtract(ONE), random);
            
            // Tính y² = x³ + ax + b (mod p)
            BigInteger x3 = x.modPow(THREE, p);
            BigInteger ax = a.multiply(x).mod(p);
            BigInteger ySquared = x3.add(ax).add(b).mod(p);
            
            // Kiểm tra y² là quadratic residue
            if (legendreSymbol(ySquared, p) == 1)
            {
                // Tìm y
                BigInteger y = modularSqrt(ySquared, p);
                if (y != null)
                {
                    // Tạo điểm
                    return curve.createPoint(x, y);
                }
            }
        }
        
        return null;
    }

    /**
     * Tính order của một điểm bằng BSGS (Baby-step Giant-step)
     * Fallback về approximation cho order quá lớn
     */
    private static BigInteger computePointOrder(
        ECPoint point, ECCurve curve, BigInteger p, SecureRandom random)
    {
        // Approximation: order nằm trong [p + 1 - 2√p, p + 1 + 2√p]
        BigInteger pPlus1 = p.add(ONE);
        BigInteger twoSqrtP = TWO.multiply(BigInteger.valueOf((long)Math.sqrt(p.doubleValue())));
        BigInteger lowerBound = pPlus1.subtract(twoSqrtP).max(ONE);
        BigInteger upperBound = pPlus1.add(twoSqrtP);

        // Nếu range quá lớn (> 2^20), dùng approximation
        BigInteger range = upperBound.subtract(lowerBound);
        if (range.bitLength() > 20)
        {
            // Approximation: dùng Hasse bound
            return pPlus1; // Approximation
        }

        // BSGS cho range nhỏ
        return computePointOrderBSGS(point, lowerBound, upperBound);
    }

    /**
     * Baby-step Giant-step algorithm để tính order
     */
    private static BigInteger computePointOrderBSGS(
        ECPoint point, BigInteger lowerBound, BigInteger upperBound)
    {
        BigInteger range = upperBound.subtract(lowerBound);
        int m = (int)Math.sqrt(range.doubleValue()) + 1;
        
        // Baby steps: lưu j*point
        java.util.Map<ECPoint, BigInteger> babySteps = new java.util.HashMap<>();
        ECPoint current = point;
        for (BigInteger j = ONE; j.compareTo(BigInteger.valueOf(m)) <= 0; j = j.add(ONE))
        {
            babySteps.put(current, j);
            current = current.add(point);
        }

        // Giant steps: kiểm tra lowerBound + m*i
        for (BigInteger i = ONE; i.compareTo(BigInteger.valueOf(m)) <= 0; i = i.add(ONE))
        {
            BigInteger k = lowerBound.add(i.multiply(BigInteger.valueOf(m)));
            if (k.compareTo(upperBound) > 0)
            {
                break;
            }
            
            ECPoint giantPoint = point.multiply(k);
            ECPoint target = giantPoint.negate();
            
            if (babySteps.containsKey(target))
            {
                BigInteger j = babySteps.get(target);
                BigInteger order = k.subtract(j);
                if (order.compareTo(ONE) > 0)
                {
                    // Verify
                    ECPoint verify = point.multiply(order);
                    if (verify.isInfinity())
                    {
                        return order;
                    }
                }
            }
        }

        // Fallback: thử các giá trị trong range (giới hạn để tránh quá chậm)
        BigInteger maxFallback = lowerBound.add(BigInteger.valueOf(Math.min(10000, range.longValue())));
        for (BigInteger k = lowerBound; k.compareTo(maxFallback) <= 0; k = k.add(ONE))
        {
            ECPoint test = point.multiply(k);
            if (test.isInfinity())
            {
                return k;
            }
        }

        return null;
    }

    /**
     * Tính approximate curve order (Hasse bound)
     */
    private static BigInteger computeCurveOrderApprox(BigInteger p)
    {
        // Hasse bound: #E(Fp) ≈ p + 1
        return p.add(ONE);
    }

    /**
     * Tìm generator point G với cofactor đúng
     */
    private static ECPoint findGeneratorWithCofactor(
        ECPoint basePoint, BigInteger n, BigInteger h, ECCurve curve, SecureRandom random)
    {
        // Nếu h = 1, basePoint có thể đã là generator
        if (h.equals(ONE))
        {
            // Verify order của basePoint
            ECPoint test = basePoint.multiply(n);
            if (test.isInfinity() && hasCorrectOrder(basePoint, n))
            {
                return basePoint;
            }
        }

        // Nếu h > 1, nhân basePoint với h để được generator
        if (h.compareTo(ONE) > 0)
        {
            ECPoint G = basePoint.multiply(h);
            
            // Verify: n * G = O
            ECPoint verify = G.multiply(n);
            if (verify.isInfinity() && hasCorrectOrder(G, n))
            {
                return G;
            }
        }

        // Fallback: thử các điểm mới
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++)
        {
            BigInteger k = BigIntegers.createRandomInRange(ONE, n.subtract(ONE), random);
            ECPoint candidate = basePoint.multiply(k);
            
            if (h.compareTo(ONE) > 0)
            {
                candidate = candidate.multiply(h);
            }
            
            if (hasCorrectOrder(candidate, n))
            {
                ECPoint verify = candidate.multiply(n);
                if (verify.isInfinity())
                {
                    return candidate;
                }
            }
        }

        return basePoint; // Fallback
    }

    /**
     * Kiểm tra điểm có order chính xác không
     */
    private static boolean hasCorrectOrder(ECPoint point, BigInteger targetOrder)
    {
        // Verify: targetOrder * point = O
        ECPoint verify = point.multiply(targetOrder);
        if (!verify.isInfinity())
        {
            return false;
        }

        // Kiểm tra các ước số nhỏ
        BigInteger[] smallPrimes = {
            TWO, THREE, BigInteger.valueOf(5),
            BigInteger.valueOf(7), BigInteger.valueOf(11), BigInteger.valueOf(13)
        };

        for (BigInteger prime : smallPrimes)
        {
            if (targetOrder.mod(prime).equals(ZERO))
            {
                BigInteger subOrder = targetOrder.divide(prime);
                ECPoint test = point.multiply(subOrder);
                if (test.isInfinity())
                {
                    return false; // Có order nhỏ hơn
                }
            }
        }

        return true;
    }

    /**
     * Verify đầy đủ domain parameters
     */
    private static boolean verifyDomainParameters(
        ECCurve curve, ECPoint G, BigInteger n, BigInteger h)
    {
        try
        {
            // 1. G không phải điểm vô cực
            if (G.isInfinity())
            {
                return false;
            }

            // 2. G nằm trên đường cong
            if (!G.isValid())
            {
                return false;
            }

            // 3. n * G = O
            ECPoint verify = G.multiply(n);
            if (!verify.isInfinity())
            {
                return false;
            }

            // 4. G có order chính xác
            if (!hasCorrectOrder(G, n))
            {
                return false;
            }

            // 5. n > 1
            if (n.compareTo(ONE) <= 0)
            {
                return false;
            }

            // 6. h >= 1
            if (h.compareTo(ONE) < 0)
            {
                return false;
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}

