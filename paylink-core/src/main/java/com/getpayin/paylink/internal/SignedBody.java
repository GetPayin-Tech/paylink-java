package com.getpayin.paylink.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a snake_case value map into the signed wire body for an endpoint: the
 * endpoint's fields, the public {@code token}, and the computed {@code signature}. The
 * signature is built from the same coerced strings that go into the body, in the spec's
 * order, skipping fields the caller did not provide — exactly how the server rebuilds it
 * from {@code Arr::except($request->validated(), ...)}.
 */
public final class SignedBody {

    private SignedBody() {
    }

    public static Map<String, String> build(EndpointSpec spec, Map<String, Object> input, String publicToken, String hashToken) {
        List<String> signedValues = new ArrayList<>();
        Map<String, String> body = new LinkedHashMap<>();

        for (FieldSpec field : spec.fields()) {
            append(body, signedValues, input.get(field.name()), field.name(), field.signed());
        }

        if (spec.countryStateBlock()) {
            appendCountryStateBlock(body, signedValues, input);
        }

        body.put("token", publicToken);
        body.put("signature", Signature.build(signedValues, hashToken));
        return body;
    }

    private static void append(Map<String, String> body, List<String> signedValues, Object value, String wireKey, boolean signed) {
        if (value == null) {
            return;
        }
        String stringValue = Coerce.toWire(value);
        body.put(wireKey, stringValue);
        if (signed) {
            signedValues.add(stringValue);
        }
    }

    private static void appendCountryStateBlock(Map<String, String> body, List<String> signedValues, Map<String, Object> input) {
        Object country = input.get("country");
        if ("US".equals(country)) {
            append(body, signedValues, input.get("us_state"), "us_state", true);
            append(body, signedValues, input.get("postal_code"), "postal_code", true);
        } else if ("CA".equals(country)) {
            append(body, signedValues, input.get("canada_state"), "canada_state", true);
            append(body, signedValues, input.get("postal_code"), "postal_code", true);
        }
    }
}
