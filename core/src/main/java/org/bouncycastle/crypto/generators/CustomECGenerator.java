package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.util.BigIntegers;

public class CustomECGenerator
{
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    private static final BigInteger TWENTY_SEVEN = BigInteger.valueOf(27);

    // =================================================================
    // PHẦN 1: SAFE PRIME GENERATION (ISO/IEC 11770-4 - Alg 3 STRICT)
    // Sinh p = 2 * q * q1 *...* qk + 1, đảm bảo qi >= q.
    // =================================================================

    public static BigInteger[] generateSafePrime(int L, int N, SecureRandom random, int certainty)
    {
        if (N <= 1) throw new IllegalArgumentException("N phai > 1");
        if (L < 2 * (N + 1)) throw new IllegalArgumentException("L phai >= 2(N+1)");

        // [Buoc 1]: q <- Random(Prime(2^(N-1), 2^N - 1))
        BigInteger minQ = ONE.shiftLeft(N - 1);
        BigInteger maxQ = ONE.shiftLeft(N).subtract(ONE);
        BigInteger q = randomPrimeInRange(minQ, maxQ, random, certainty);
        
        if (q == null) throw new IllegalStateException("FATAL: Khong tim duoc q trong khoang N-bit");

        // [Buoc 2]: f0 <- 2*q
        BigInteger f = q.shiftLeft(1);
        int M = f.bitLength();

        // [Buoc 3]: k
        int maxK = (L - M - 1) / N;
        if (maxK < 1) maxK = 1;
        int k = 1 + random.nextInt(maxK);

        // [Buoc 4-5]: Sinh k-1 so nguyen to qi
        for (int i = 1; i < k; i++)
        {
            // 5.1: Ni random
            int remainingBits = L - M - (k - i) * N - 1;
            int lowerNi = N;
            int upperNi = remainingBits < lowerNi ? lowerNi : remainingBits;
            
            int Ni = lowerNi + random.nextInt(upperNi - lowerNi + 1);

            BigInteger lowerQi, upperQi;
            
            // 5.2 & 5.3: Xac dinh range cho qi
            if (Ni == N) {
                lowerQi = q; // Dam bao qi >= q
                upperQi = ONE.shiftLeft(N).subtract(ONE);
            } else {
                lowerQi = ONE.shiftLeft(Ni - 1);
                upperQi = ONE.shiftLeft(Ni).subtract(ONE);
            }

            BigInteger qi = randomPrimeInRange(lowerQi, upperQi, random, certainty);
            if (qi == null) throw new IllegalStateException("Khong tim duoc qi tai buoc i=" + i);
            
            // [Check]: Strict condition qi >= q
            if (qi.compareTo(q) < 0) {
                throw new IllegalStateException("ALGORITHM FAIL: qi < q.");
            }

            // 5.4: f <- f * qi
            f = f.multiply(qi);
            M = f.bitLength();
        }

        // [Buoc 6]: Tinh khoang [A, B] cho qk
        BigInteger minP = ONE.shiftLeft(L - 1);
        BigInteger maxP = ONE.shiftLeft(L).subtract(ONE);
        
        BigInteger A = minP.divide(f);
        if (minP.mod(f).signum() > 0) A = A.add(ONE); // Ceil
        BigInteger B = maxP.divide(f);

        if (A.compareTo(B) > 0) throw new IllegalStateException("Khoang [A, B] rong.");

        // [Buoc 7 & 9]: Quet qk (Circular Scan Corrected)
        BigInteger range = B.subtract(A).add(ONE);
        BigInteger startOffset = new BigInteger(range.bitLength(), random).mod(range);
        
        // Khoi tao startQk la so le
        BigInteger startQk = A.add(startOffset);
        if (!startQk.testBit(0)) startQk = startQk.add(ONE);
        
        // Dam bao startQk nam trong [A, B] sau khi chinh le
        if (startQk.compareTo(B) > 0) {
            startQk = A.testBit(0) ? A : A.add(ONE);
        }

        BigInteger qk = startQk;
        boolean wrapped = false;

        // Loop quet wrap-around
        while (true) {
            // Check prime
            if (qk.isProbablePrime(certainty)) {
                BigInteger p = f.multiply(qk).add(ONE);
                
                if (p.bitLength() == L && p.isProbablePrime(certainty)) {
                    if (qk.compareTo(q) >= 0) {
                        // [Verify Final]: Defense-in-depth
                        BigInteger pMinus1 = p.subtract(ONE);
                        if (!pMinus1.mod(f).equals(ZERO)) throw new IllegalStateException("Check Fail: f not divide p-1");
                        if (!pMinus1.mod(q).equals(ZERO)) throw new IllegalStateException("Check Fail: q not divide p-1");
                        
                        return new BigInteger[]{p, q};
                    }
                }
            }

            // Next candidate (+2)
            qk = qk.add(TWO);
            
            // Wrap around logic
            if (qk.compareTo(B) > 0) {
                qk = A;
                if (!qk.testBit(0)) qk = qk.add(ONE);
                wrapped = true; 
            }

            // [STOP CONDITION]: Check dung sau khi da Next va Wrap
            // Neu quay lai dung startQk thi dung (da quet het 1 vong)
            if (wrapped && qk.equals(startQk)) {
                throw new IllegalStateException("Da duyet het khoang [A, B] ma khong tim duoc safe prime p.");
            }
        }
    }

    /**
     * Thuật toán 1: Random(Prime(A,B)) - Logic quet chuan
     */
    private static BigInteger randomPrimeInRange(BigInteger min, BigInteger max, SecureRandom random, int certainty) {
        if (min.compareTo(max) > 0) return null;
        
        BigInteger range = max.subtract(min).add(ONE);
        BigInteger startOffset = new BigInteger(range.bitLength(), random).mod(range);
        
        // Start p (le)
        BigInteger startP = min.add(startOffset);
        if (!startP.testBit(0)) startP = startP.add(ONE);
        if (startP.compareTo(max) > 0) {
            startP = min.testBit(0) ? min : min.add(ONE);
        }
        
        BigInteger p = startP;
        boolean wrapped = false;

        while (true) {
            // Check prime
            if (p.isProbablePrime(certainty)) {
                return p;
            }
            
            // Next (+2)
            p = p.add(TWO);
            
            // Wrap
            if (p.compareTo(max) > 0) {
                p = min;
                if (!p.testBit(0)) p = p.add(ONE);
                wrapped = true;
            }

            // [STOP CONDITION]: Check dung sau khi da Next va Wrap
            if (wrapped && p.equals(startP)) {
                return null; // Khong co so nguyen to trong khoang
            }
        }
    }

    // =================================================================
    // PHẦN 2: VERIFIABLE RANDOM CURVE (ANSI X9.62 Fixed)
    // =================================================================

    public static RawCurveData generateRawCurve(BigInteger p, SecureRandom random) {
        int t = p.bitLength();
        int s = (t - 1) / 160; 
        int v = t - 160 * s;
        int g = 160;
        byte[] seedE = new byte[g/8];
        
        // ANSI X9.62: Fix a = -3 (mod p) for efficiency
        BigInteger a = p.subtract(THREE);

        while (true) {
            random.nextBytes(seedE);
            
            // 1. Sinh r tu seed
            BigInteger r = hashSeedToR(seedE, p, s, v, g);
            
            if (r.signum() == 0) continue;
            // [Check]: r < p (Paper-friendly check)
            if (r.compareTo(p) >= 0) continue;

            // 2. Tinh b: r * b^2 = a^3 -> b^2 = a^3 * r^-1
            BigInteger rInv = r.modInverse(p);
            BigInteger a3 = a.modPow(THREE, p);
            BigInteger rhs = a3.multiply(rInv).mod(p);

            // Check Legendre (co phai so chinh phuong khong)
            if (legendre(rhs, p) != 1) continue;

            // Tinh can bac hai: b = sqrt(rhs)
            BigInteger b = modularSqrt(rhs, p);
            
            // [Safety Check]: Defensive null check
            if (b == null) continue;
            
            // 3. Check Discriminant: 4a^3 + 27b^2 != 0
            BigInteger term1 = FOUR.multiply(a3).mod(p);
            BigInteger b2 = b.modPow(TWO, p);
            BigInteger term2 = TWENTY_SEVEN.multiply(b2).mod(p);
            BigInteger disc = term1.add(term2).mod(p);

            if (disc.equals(ZERO)) continue;

            return new RawCurveData(p, a, b, seedE);
        }
    }

    public static boolean verifyRandomCurveFp(BigInteger p, byte[] seedE, BigInteger a, BigInteger b) {
        try {
            // 1. Check a = -3
            BigInteger expectedA = p.subtract(THREE);
            if (!a.equals(expectedA)) return false;

            int t = p.bitLength();
            int s = (t - 1) / 160; 
            int v = t - 160 * s;
            int g = seedE.length * 8;
            
            // 2. Re-compute r
            BigInteger r = hashSeedToR(seedE, p, s, v, g);
            if (r.signum() == 0 || r.compareTo(p) >= 0) return false;

            // 3. Check equation: r * b^2 == a^3
            BigInteger b2 = b.modPow(TWO, p);
            BigInteger lhs = r.multiply(b2).mod(p);
            BigInteger rhs = a.modPow(THREE, p);

            return lhs.equals(rhs);
        } catch (Exception e) {
            return false;
        }
    }

    private static BigInteger hashSeedToR(byte[] seedE, BigInteger p, int s, int v, int g) {
        byte[] H = sha1(seedE);
        BigInteger c0 = new BigInteger(1, H);
        BigInteger maskV = ONE.shiftLeft(v).subtract(ONE);
        BigInteger W0 = c0.and(maskV);
        if (v > 0) W0 = W0.clearBit(v - 1); 

        BigInteger z = new BigInteger(1, seedE);
        BigInteger W = W0;
        
        for (int i = 1; i <= s; i++) {
            BigInteger si_val = z.add(BigInteger.valueOf(i)).mod(ONE.shiftLeft(g));
            byte[] si_bytes = BigIntegers.asUnsignedByteArray(g/8, si_val);
            BigInteger Wi = new BigInteger(1, sha1(si_bytes));
            W = W.shiftLeft(160).add(Wi);
        }
        return W;
    }

    // =================================================================
    // PHẦN 3: MATH UTILS (Robust Tonelli-Shanks)
    // =================================================================

    /**
     * Tonelli-Shanks voi kieu int cho so mu (safe implementation)
     */
    public static BigInteger modularSqrt(BigInteger a, BigInteger p) {
        if (legendre(a, p) != 1) return null;
        if (a.equals(ZERO)) return ZERO;
        if (p.equals(TWO)) return a;

        // Case p = 3 mod 4
        if (p.mod(FOUR).equals(THREE)) {
            return a.modPow(p.add(ONE).shiftRight(2), p);
        }

        // Case p = 1 mod 4 (Tonelli-Shanks)
        // 1. p - 1 = q * 2^s
        int s = 0;
        BigInteger q = p.subtract(ONE);
        while (!q.testBit(0)) {
            s++;
            q = q.shiftRight(1);
        }
        
        // 2. Tim z non-residue
        BigInteger z = TWO;
        while (legendre(z, p) != -1) z = z.add(ONE);

        // 3. Khoi tao
        BigInteger c = z.modPow(q, p);
        BigInteger r = a.modPow(q.add(ONE).shiftRight(1), p);
        BigInteger t = a.modPow(q, p);
        int m = s;

        // 4. Loop
        while (!t.equals(ONE)) {
            BigInteger tt = t;
            int i = 0;
            
            // Tim i nho nhat de t^(2^i) = 1
            while (!tt.equals(ONE)) {
                tt = tt.modPow(TWO, p);
                i++;
                if (i == m) return null; // Should not happen
            }
            
            // Tinh b = c^(2^(m-i-1))
            // Dung int cho so mu de tranh overflow/logic sai voi BigInteger
            int e = m - i - 1;
            BigInteger b = c.modPow(ONE.shiftLeft(e), p);
            
            m = i;
            c = b.modPow(TWO, p); // c = b^2
            t = t.multiply(c).mod(p);
            r = r.multiply(b).mod(p);
        }
        return r;
    }

    private static int legendre(BigInteger a, BigInteger p) {
        if (a.mod(p).equals(ZERO)) return 0;
        BigInteger res = a.modPow(p.subtract(ONE).shiftRight(1), p);
        if (res.equals(ONE)) return 1;
        if (res.equals(p.subtract(ONE))) return -1;
        return 0;
    }

    private static byte[] sha1(byte[] input) {
        SHA1Digest d = new SHA1Digest();
        d.update(input, 0, input.length);
        byte[] out = new byte[d.getDigestSize()];
        d.doFinal(out, 0);
        return out;
    }

    public static class RawCurveData {
        public final BigInteger p, a, b;
        public final byte[] seedE;
        public RawCurveData(BigInteger p, BigInteger a, BigInteger b, byte[] seed) {
            this.p = p; this.a = a; this.b = b; this.seedE = seed;
        }
    }
}
