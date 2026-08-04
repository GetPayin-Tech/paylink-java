package com.getpayin.paylink.internal;

import com.getpayin.paylink.exception.PaylinkException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Canonical value → string coercion, used for BOTH the signature concatenation and
 * the JSON request body, so the bytes signed are exactly the bytes sent. {@code null}
 * becomes an empty string, matching PHP's {@code implode}. Integer types render without
 * grouping, so a machine's locale never shifts a signature.
 */
public final class Coerce {

    private Coerce() {
    }

    public static String toWire(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof Boolean b) {
            return b ? "1" : "0";
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return value.toString();
        }
        if (value instanceof BigInteger bi) {
            return bi.toString();
        }
        if (value instanceof BigDecimal bd) {
            return bd.toPlainString();
        }
        if (value instanceof Double || value instanceof Float) {
            return formatFloating(((Number) value).doubleValue());
        }
        throw new PaylinkException("Cannot serialize value of type " + value.getClass().getName() + ".");
    }

    private static String formatFloating(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new PaylinkException("Cannot serialize non-finite number: " + value);
        }
        if (value == Math.floor(value) && Math.abs(value) < 9.2e18) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
