package com.getpayin.paylink.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code invoices().create(...)}. {@code redirectionUrl}/{@code webhookUrl}
 * must be HTTPS URLs on the integration's registered domain; {@code paymentMode}
 * ({@code capture} or {@code authorize}) is sent but excluded from the signature. Pass
 * {@code orderAmount} as a string to control the exact wire form.
 */
public final class CreateInvoiceParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public CreateInvoiceParams firstName(String value) {
        fields.put("first_name", value);
        return this;
    }

    public CreateInvoiceParams lastName(String value) {
        fields.put("last_name", value);
        return this;
    }

    public CreateInvoiceParams email(String value) {
        fields.put("email", value);
        return this;
    }

    public CreateInvoiceParams orderTitle(String value) {
        fields.put("order_title", value);
        return this;
    }

    public CreateInvoiceParams orderAmount(String value) {
        fields.put("order_amount", value);
        return this;
    }

    public CreateInvoiceParams currency(String value) {
        fields.put("currency", value);
        return this;
    }

    public CreateInvoiceParams address(String value) {
        fields.put("address", value);
        return this;
    }

    public CreateInvoiceParams city(String value) {
        fields.put("city", value);
        return this;
    }

    public CreateInvoiceParams country(String value) {
        fields.put("country", value);
        return this;
    }

    public CreateInvoiceParams state(String value) {
        fields.put("state", value);
        return this;
    }

    public CreateInvoiceParams redirectionUrl(String value) {
        fields.put("redirection_url", value);
        return this;
    }

    public CreateInvoiceParams webhookUrl(String value) {
        fields.put("webhook_url", value);
        return this;
    }

    public CreateInvoiceParams orderDetails(String value) {
        fields.put("order_details", value);
        return this;
    }

    public CreateInvoiceParams paymentMode(String value) {
        fields.put("payment_mode", value);
        return this;
    }

    /** Internal: the accumulated snake_case fields. */
    public Map<String, Object> fields() {
        return fields;
    }
}
