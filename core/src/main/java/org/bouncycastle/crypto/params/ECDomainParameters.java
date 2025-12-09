package org.bouncycastle.crypto.params;

import java.math.BigInteger;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;

public class ECDomainParameters
    implements ECConstants
{
    private final ECCurve     curve;
    private final byte[]      seed;
    private final ECPoint     G;
    private final BigInteger  n;
    private final BigInteger  h;

    private BigInteger  hInv = null;

    public ECDomainParameters(X9ECParameters x9)
    {
        this(x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
    }

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n)
    {
        this(curve, G, n, ONE, null);
    }

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h)
    {
        this(curve, G, n, h, null);
    }

    public ECDomainParameters(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h,
        byte[]      seed)
    {
        if (curve == null)
        {
            throw new NullPointerException("curve");
        }
        if (n == null)
        {
            throw new NullPointerException("n");
        }
        // we can't check for h == null here as h is optional in X9.62 as it is not required for ECDSA

        this.curve = curve;
        this.G = validatePublicPoint(curve, G);
        this.n = n;
        this.h = h;
        this.seed = Arrays.clone(seed);
    }

    public String getDetailInfo()
    {
        String out = "";
        if (curve != null)
        {
            out = out + "curve: " + curve.getClass().getName() + "\n";
        }
        else
        {
            out = out + "curve: null\n";
        }

        if (n != null)
        {
            out = out + "n: " + Hex.toHexString(n.toByteArray()) + "\n";
        }
        else
        {
            out = out + "n: null\n";
        }

        if (h != null)
        {
            out = out + "h: " + Hex.toHexString(h.toByteArray()) + "\n";
        }
        else
        {
            out = out + "h: null\n";
        }

        if (seed != null)
        {
            out = out + "seed: " + Hex.toHexString(seed) + "\n";
        }
        else
        {
            out = out + "seed: null\n";
        }
                     
        if (G != null)
        {
            out = out + "G no compressed: " + Hex.toHexString(G.getEncoded(false)) + "\n" +
                        "G compressed: " + Hex.toHexString(G.getEncoded(true)) + "\n";
        }
        else
        {
            out = out + "G: null\n";
        }

        if (curve instanceof ECCurve.Fp)
        {
            ECCurve.Fp fpCurve = (ECCurve.Fp)curve;
            if (fpCurve.getQ() != null)
            {
                out = out + "fpCurve.q: " + Hex.toHexString(fpCurve.getQ().toByteArray()) + "\n";
            }
            else
            {
                out = out + "fpCurve.q: null\n";
            }

            if (fpCurve.getA() != null)
            {
                out = out + "fpCurve.a: " + Hex.toHexString(fpCurve.getA().toBigInteger().toByteArray()) +"\n";
            }
            else
            {
                out = out + "fpCurve.a: null\n";
            }

            if (fpCurve.getB() != null)
            {
                out = out + "fpCurve.b: " + Hex.toHexString(fpCurve.getB().toBigInteger().toByteArray()) +"\n";
            }
            else
            {
                out = out + "fpCurve.b: null\n";
            }

            if (fpCurve.getOrder() != null)
            {
                out = out + "fpCurve.order: " + Hex.toHexString(fpCurve.getOrder().toByteArray()) +"\n";
            }
            else
            {
                out = out + "fpCurve.order: null\n";
            }

            if (fpCurve.getCofactor() != null)
            {
                out = out + "fpCurve.cofactor: " + Hex.toHexString(fpCurve.getCofactor().toByteArray()) +"\n";
            }
            else
            {
                out = out + "fpCurve.cofactor: null\n";
            }
        }

        return out;
    }

    public ECCurve getCurve()
    {
        return curve;
    }

    public ECPoint getG()
    {
        return G;
    }

    public BigInteger getN()
    {
        return n;
    }

    public BigInteger getH()
    {
        return h;
    }

    public synchronized BigInteger getHInv()
    {
        if (hInv == null)
        {
            hInv = BigIntegers.modOddInverseVar(n, h);
        }
        return hInv;
    }

    public byte[] getSeed()
    {
        return Arrays.clone(seed);
    }

    public boolean equals(
        Object  obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof ECDomainParameters))
        {
            return false;
        }

        ECDomainParameters other = (ECDomainParameters)obj;

        return this.curve.equals(other.curve)
            && this.G.equals(other.G)
            && this.n.equals(other.n);
    }

    public int hashCode()
    {
//        return Arrays.hashCode(new Object[]{ curve, G, n });
        int hc = 4;
        hc *= 257;
        hc ^= curve.hashCode();
        hc *= 257;
        hc ^= G.hashCode();
        hc *= 257;
        hc ^= n.hashCode();
        return hc;
    }

    public BigInteger validatePrivateScalar(BigInteger d)
    {
        if (null == d)
        {
            throw new NullPointerException("Scalar cannot be null");
        }

        if (d.compareTo(ECConstants.ONE) < 0  || (d.compareTo(getN()) >= 0))
        {
            throw new IllegalArgumentException("Scalar is not in the interval [1, n - 1]");
        }

        return d;
    }

    public ECPoint validatePublicPoint(ECPoint q)
    {
        return validatePublicPoint(getCurve(), q);
    }

    static ECPoint validatePublicPoint(ECCurve c, ECPoint q)
    {
        if (null == q)
        {
            throw new NullPointerException("Point cannot be null");
        }

        q = ECAlgorithms.importPoint(c, q).normalize();

        if (q.isInfinity())
        {
            throw new IllegalArgumentException("Point at infinity");
        }

        if (!q.isValid())
        {
            throw new IllegalArgumentException("Point not on curve");
        }

        return q;
    }
}
