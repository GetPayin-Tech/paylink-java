package com.getpayin.paylink.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code vcc().charge(...)}. For {@code country("US")} pass
 * {@code usState} + {@code postalCode}; for {@code "CA"} pass {@code canadaState} +
 * {@code postalCode}.
 */
public final class VccChargeParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public VccChargeParams firstName(String value) {
        fields.put("first_name", value);
        return this;
    }

    public VccChargeParams lastName(String value) {
        fields.put("last_name", value);
        return this;
    }

    public VccChargeParams email(String value) {
        fields.put("email", value);
        return this;
    }

    public VccChargeParams phone(String value) {
        fields.put("phone", value);
        return this;
    }

    public VccChargeParams currencyId(long value) {
        fields.put("currency_id", value);
        return this;
    }

    public VccChargeParams price(String value) {
        fields.put("price", value);
        return this;
    }

    public VccChargeParams product(String value) {
        fields.put("product", value);
        return this;
    }

    public VccChargeParams referenceNumber(String value) {
        fields.put("reference_number", value);
        return this;
    }

    public VccChargeParams cardNumber(String value) {
        fields.put("card_number", value);
        return this;
    }

    public VccChargeParams cardExpiryMonth(String value) {
        fields.put("card_expiry_month", value);
        return this;
    }

    public VccChargeParams cardExpiryYear(String value) {
        fields.put("card_expiry_year", value);
        return this;
    }

    public VccChargeParams cardCvv(String value) {
        fields.put("card_cvv", value);
        return this;
    }

    public VccChargeParams country(String value) {
        fields.put("country", value);
        return this;
    }

    public VccChargeParams address(String value) {
        fields.put("address", value);
        return this;
    }

    public VccChargeParams city(String value) {
        fields.put("city", value);
        return this;
    }

    public VccChargeParams usState(String value) {
        fields.put("us_state", value);
        return this;
    }

    public VccChargeParams canadaState(String value) {
        fields.put("canada_state", value);
        return this;
    }

    public VccChargeParams postalCode(String value) {
        fields.put("postal_code", value);
        return this;
    }

    /** Internal: the accumulated snake_case fields. */
    public Map<String, Object> fields() {
        return fields;
    }
}
