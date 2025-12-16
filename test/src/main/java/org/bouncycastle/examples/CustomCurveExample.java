package org.bouncycastle.examples;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import org.bouncycastle.crypto.generators.CustomCurveManager;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

/**
 * Vi du: Sinh, verify, luu va su dung lai duong cong
 * 
 * Production Workflow (one-button):
 * 1. generateAndRegisterCurve() -> tự động:
 *    - PHASE A: Sinh raw curve (p, a, b, seed)
 *    - PHASE B: Gọi SageMath tính order n, cofactor h, generator G
 *    - PHASE C: Finalize và verify strict (h=1, n prime, n*G=O)
 * 2. Su dung nhu named curve de ky va xac thuc
 */
public class CustomCurveExample
{
    public static void main(String[] args) throws Exception
    {
        // Dang ky Bouncy Castle Provider
        Security.addProvider(new BouncyCastleProvider());
        
        SecureRandom random = new SecureRandom();
        
        System.out.println("=== Custom Curve ECDSA Example (Production Workflow) ===");
        System.out.println();
        
        String curveName = "MyCustomCurve-256";
        int L = 256;           // bit length of p
        int N = 127;           // bit length of q (the q in safe prime)
        int maxAttempts = 500; // increase retries because Ncurve prime is rare
        String sageScriptPath = "D:/Build_ECDSA/test/scripts/compute_order_complete.py"; // absolute path
        
        // PRODUCTION WORKFLOW: One-button generation
        System.out.println("Generating curve with h=1 requirement...");
        System.out.println("(This may take several attempts as Ncurve prime is rare)");
        System.out.println();
        
        ECDomainParameters params = CustomCurveManager.generateAndRegisterCurve(
            curveName, L, N, maxAttempts, sageScriptPath);
        
        if (params == null) {
            System.err.println("Failed to generate curve after " + maxAttempts + " attempts.");
            System.err.println("Try increasing maxAttempts or using different parameters.");
            return;
        }
        
        System.out.println();
        System.out.println("=== Using Generated Curve for ECDSA ===");
        System.out.println();
        
        // Convert to JCE format
        ECParameterSpec spec = new ECParameterSpec(
            params.getCurve(),
            params.getG(),
            params.getN(),
            params.getH(),
            params.getSeed()
        );
        
        // Generate key pair
        System.out.println("Generating ECDSA key pair...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(spec, random);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        System.out.println("Key pair generated successfully!");
        System.out.println();
        
        // Test signing
        System.out.println("Testing ECDSA signing and verification...");
        byte[] message = "Hello Custom Curve ECDSA!".getBytes();
        
        Signature signer = Signature.getInstance("SHA256withECDSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update(message);
        byte[] signature = signer.sign();
        
        System.out.println("Message: " + new String(message));
        System.out.println("Signature length: " + signature.length + " bytes");
        System.out.println();
        
        // Verify signature
        Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(message);
        boolean valid = verifier.verify(signature);
        
        if (valid) {
            System.out.println("Signature verification: SUCCESS");
        } else {
            System.out.println("Signature verification: FAILED");
        }
        
        System.out.println();
        System.out.println("=== Example completed successfully! ===");
    }
}
