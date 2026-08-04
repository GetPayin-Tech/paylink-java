package com.getpayin.paylink.internal;

import com.getpayin.paylink.exception.PaylinkException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC-SHA256 signing primitive, byte-compatible with the server. Mirrors
 * {@code ExternalPaymentIntegration::buildSignatureString()}:
 * {@code base64_encode(hash_hmac('sha256', implode('', $data), $hashToken, true))}.
 */
public final class Signature {

    private Signature() {
    }

    /**
     * HMAC-SHA256 over the empty-string join of the already-ordered values, base64-encoded.
     * Callers supply values in exact wire order, coerced with {@link Coerce}.
     */
    public static String build(List<String> orderedValues, String hashToken) {
        String concatenated = String.join("", orderedValues);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hashToken.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(concatenated.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new PaylinkException("Failed to compute HMAC signature.", e);
        }
    }

    /** Constant-time comparison, equivalent to PHP's {@code hash_equals}. */
    public static boolean signaturesEqual(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
