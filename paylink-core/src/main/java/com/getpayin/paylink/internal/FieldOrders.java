package com.getpayin.paylink.internal;

import java.util.List;

/**
 * The signed-field registry. Each entry lists a request's fields in the EXACT order
 * the server concatenates them when it rebuilds the HMAC signature (the FormRequest
 * {@code rules()} order, minus {@code token}/{@code signature}, and minus
 * {@code payment_mode} which is sent but not signed). Keep it in lockstep with the
 * server FormRequests under {@code app/Http/Requests/Application/ExternalPaymentIntegration}.
 */
public final class FieldOrders {

    private FieldOrders() {
    }

    public static final EndpointSpec INVOICE_CREATE = new EndpointSpec(
            "/api/v2/integration/init",
            List.of(
                    FieldSpec.of("first_name"),
                    FieldSpec.of("last_name"),
                    FieldSpec.of("email"),
                    FieldSpec.of("order_title"),
                    FieldSpec.of("order_amount"),
                    FieldSpec.of("address"),
                    FieldSpec.of("city"),
                    FieldSpec.of("country"),
                    FieldSpec.of("state"),
                    FieldSpec.of("currency"),
                    FieldSpec.of("redirection_url"),
                    FieldSpec.of("webhook_url"),
                    FieldSpec.of("order_details"),
                    FieldSpec.unsigned("payment_mode")),
            false);

    public static final EndpointSpec PAYMENT_VOID = new EndpointSpec(
            "/api/integration/void", List.of(FieldSpec.of("invoice_id")), false);

    public static final EndpointSpec PAYMENT_REFUND = new EndpointSpec(
            "/api/integration/refund", List.of(FieldSpec.of("invoice_id"), FieldSpec.of("amount")), false);

    public static final EndpointSpec PAYMENT_SETTLE = new EndpointSpec(
            "/api/integration/settle", List.of(FieldSpec.of("invoice_id"), FieldSpec.of("amount")), false);

    public static final EndpointSpec PAYMENT_REVERSE_AUTHORIZATION = new EndpointSpec(
            "/api/integration/reverse-authorization", List.of(FieldSpec.of("invoice_id")), false);

    public static final EndpointSpec PAYMENT_CHECK_STATUS = new EndpointSpec(
            "/api/integration/check-status", List.of(FieldSpec.of("invoice_id")), false);

    public static final EndpointSpec VCC_CHARGE = new EndpointSpec(
            "/api/v2/integration/vcc/charge",
            List.of(
                    FieldSpec.of("first_name"),
                    FieldSpec.of("last_name"),
                    FieldSpec.of("email"),
                    FieldSpec.of("phone"),
                    FieldSpec.of("currency_id"),
                    FieldSpec.of("price"),
                    FieldSpec.of("product"),
                    FieldSpec.of("reference_number"),
                    FieldSpec.of("card_number"),
                    FieldSpec.of("card_expiry_month"),
                    FieldSpec.of("card_expiry_year"),
                    FieldSpec.of("card_cvv"),
                    FieldSpec.of("country"),
                    FieldSpec.of("address"),
                    FieldSpec.of("city")),
            true);

    public static final EndpointSpec CARD_TOKENIZE = new EndpointSpec(
            "/api/v2/integration/tokens/card",
            List.of(
                    FieldSpec.of("first_name"),
                    FieldSpec.of("last_name"),
                    FieldSpec.of("email"),
                    FieldSpec.of("customer_reference"),
                    FieldSpec.of("external_reference"),
                    FieldSpec.of("card_number"),
                    FieldSpec.of("card_expiry_month"),
                    FieldSpec.of("card_expiry_year"),
                    FieldSpec.of("card_cvv"),
                    FieldSpec.of("country"),
                    FieldSpec.of("address"),
                    FieldSpec.of("city")),
            true);

    public static final EndpointSpec CARD_CHARGE = new EndpointSpec(
            "/api/v2/integration/tokens/charge",
            List.of(
                    FieldSpec.of("card_token"),
                    FieldSpec.of("initiator"),
                    FieldSpec.of("first_name"),
                    FieldSpec.of("last_name"),
                    FieldSpec.of("email"),
                    FieldSpec.of("currency"),
                    FieldSpec.of("price"),
                    FieldSpec.of("product"),
                    FieldSpec.of("reference_number"),
                    FieldSpec.of("country"),
                    FieldSpec.of("address"),
                    FieldSpec.of("city")),
            true);

    public static final EndpointSpec CARD_REVOKE = new EndpointSpec(
            "/api/v2/integration/tokens/revoke", List.of(FieldSpec.of("card_token")), false);

    public static final EndpointSpec RECURRING_CREATE = new EndpointSpec(
            "/api/v2/integration/recurring/init",
            List.of(
                    FieldSpec.of("first_name"),
                    FieldSpec.of("last_name"),
                    FieldSpec.of("email"),
                    FieldSpec.of("order_title"),
                    FieldSpec.of("order_amount"),
                    FieldSpec.of("currency"),
                    FieldSpec.of("cadence_interval"),
                    FieldSpec.of("cadence_count"),
                    FieldSpec.of("total_cycles"),
                    FieldSpec.of("end_date"),
                    FieldSpec.of("consent_text"),
                    FieldSpec.of("external_reference"),
                    FieldSpec.of("redirection_url"),
                    FieldSpec.of("webhook_url")),
            false);
}
