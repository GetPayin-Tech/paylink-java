package com.getpayin.paylink.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for {@code cards().tokenize(...)}. See {@link VccChargeParams} for the state fields. */
public final class TokenizeCardParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public TokenizeCardParams firstName(String value) {
        fields.put("first_name", value);
        return this;
    }

    public TokenizeCardParams lastName(String value) {
        fields.put("last_name", value);
        return this;
    }

    public TokenizeCardParams email(String value) {
        fields.put("email", value);
        return this;
    }

    public TokenizeCardParams customerReference(String value) {
        fields.put("customer_reference", value);
        return this;
    }

    public TokenizeCardParams externalReference(String value) {
        fields.put("external_reference", value);
        return this;
    }

    public TokenizeCardParams cardNumber(String value) {
        fields.put("card_number", value);
        return this;
    }

    public TokenizeCardParams cardExpiryMonth(String value) {
        fields.put("card_expiry_month", value);
        return this;
    }

    public TokenizeCardParams cardExpiryYear(String value) {
        fields.put("card_expiry_year", value);
        return this;
    }

    public TokenizeCardParams cardCvv(String value) {
        fields.put("card_cvv", value);
        return this;
    }

    public TokenizeCardParams country(String value) {
        fields.put("country", value);
        return this;
    }

    public TokenizeCardParams address(String value) {
        fields.put("address", value);
        return this;
    }

    public TokenizeCardParams city(String value) {
        fields.put("city", value);
        return this;
    }

    public TokenizeCardParams usState(String value) {
        fields.put("us_state", value);
        return this;
    }

    public TokenizeCardParams canadaState(String value) {
        fields.put("canada_state", value);
        return this;
    }

    public TokenizeCardParams postalCode(String value) {
        fields.put("postal_code", value);
        return this;
    }

    /** Internal: the accumulated snake_case fields. */
    public Map<String, Object> fields() {
        return fields;
    }
}
