package com.getpayin.paylink.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parameters for {@code recurring().create(...)}. {@code cadenceInterval} is one of
 * {@code day}/{@code week}/{@code month}/{@code year}; {@code endDate} is
 * {@code YYYY-MM-DD} and must be after today.
 */
public final class CreateRecurringParams {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public CreateRecurringParams firstName(String value) {
        fields.put("first_name", value);
        return this;
    }

    public CreateRecurringParams lastName(String value) {
        fields.put("last_name", value);
        return this;
    }

    public CreateRecurringParams email(String value) {
        fields.put("email", value);
        return this;
    }

    public CreateRecurringParams orderTitle(String value) {
        fields.put("order_title", value);
        return this;
    }

    public CreateRecurringParams orderAmount(String value) {
        fields.put("order_amount", value);
        return this;
    }

    public CreateRecurringParams currency(String value) {
        fields.put("currency", value);
        return this;
    }

    public CreateRecurringParams cadenceInterval(String value) {
        fields.put("cadence_interval", value);
        return this;
    }

    public CreateRecurringParams cadenceCount(long value) {
        fields.put("cadence_count", value);
        return this;
    }

    public CreateRecurringParams consentText(String value) {
        fields.put("consent_text", value);
        return this;
    }

    public CreateRecurringParams totalCycles(long value) {
        fields.put("total_cycles", value);
        return this;
    }

    public CreateRecurringParams endDate(String value) {
        fields.put("end_date", value);
        return this;
    }

    public CreateRecurringParams externalReference(String value) {
        fields.put("external_reference", value);
        return this;
    }

    public CreateRecurringParams redirectionUrl(String value) {
        fields.put("redirection_url", value);
        return this;
    }

    public CreateRecurringParams webhookUrl(String value) {
        fields.put("webhook_url", value);
        return this;
    }

    /** Internal: the accumulated snake_case fields. */
    public Map<String, Object> fields() {
        return fields;
    }
}
