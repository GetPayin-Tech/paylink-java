package com.getpayin.paylink.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Parameters for {@code cards().charge(...)}. {@code initiator} is {@code merchant} or {@code customer}. */
public final class ChargeCardParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public ChargeCardParams cardToken(String value) {
        fields.put("card_token", value);
        return this;
    }

    public ChargeCardParams initiator(String value) {
        fields.put("initiator", value);
        return this;
    }

    public ChargeCardParams firstName(String value) {
        fields.put("first_name", value);
        return this;
    }

    public ChargeCardParams lastName(String value) {
        fields.put("last_name", value);
        return this;
    }

    public ChargeCardParams email(String value) {
        fields.put("email", value);
        return this;
    }

    public ChargeCardParams currency(String value) {
        fields.put("currency", value);
        return this;
    }

    public ChargeCardParams price(String value) {
        fields.put("price", value);
        return this;
    }

    public ChargeCardParams product(String value) {
        fields.put("product", value);
        return this;
    }

    public ChargeCardParams referenceNumber(String value) {
        fields.put("reference_number", value);
        return this;
    }

    public ChargeCardParams country(String value) {
        fields.put("country", value);
        return this;
    }

    public ChargeCardParams address(String value) {
        fields.put("address", value);
        return this;
    }

    public ChargeCardParams city(String value) {
        fields.put("city", value);
        return this;
    }

    public ChargeCardParams usState(String value) {
        fields.put("us_state", value);
        return this;
    }

    public ChargeCardParams canadaState(String value) {
        fields.put("canada_state", value);
        return this;
    }

    public ChargeCardParams postalCode(String value) {
        fields.put("postal_code", value);
        return this;
    }

    /** Internal: the accumulated snake_case fields. */
    public Map<String, Object> fields() {
        return fields;
    }
}
