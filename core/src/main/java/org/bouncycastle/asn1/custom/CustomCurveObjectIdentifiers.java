package org.bouncycastle.asn1.custom;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * OIDs for custom curves.
 * 99999 is for lab/demo. Replace with a real PEN (IANA) for production.
 */
public final class CustomCurveObjectIdentifiers
{
    private CustomCurveObjectIdentifiers()
    {
    }

    private static final ASN1ObjectIdentifier base = new ASN1ObjectIdentifier("1.3.6.1.4.1");
    private static final ASN1ObjectIdentifier enterprise = base.branch("99999");

    public static final ASN1ObjectIdentifier myCustomCurve256 = enterprise.branch("1");
}

