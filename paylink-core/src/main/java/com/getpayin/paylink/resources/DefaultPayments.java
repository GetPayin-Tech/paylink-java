package com.getpayin.paylink.resources;

import com.getpayin.paylink.http.RequestExecutor;
import com.getpayin.paylink.http.RequestOptions;
import com.getpayin.paylink.internal.EndpointSpec;
import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.PaymentResult;
import com.getpayin.paylink.model.RefundResult;
import com.google.gson.JsonElement;
import java.util.Map;

/** Default {@link Payments} implementation. */
public final class DefaultPayments implements Payments {

    private final PaylinkConfig config;

    public DefaultPayments(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public PaymentResult voidInvoice(long invoiceId) {
        return voidInvoice(invoiceId, null);
    }

    @Override
    public PaymentResult voidInvoice(long invoiceId, String idempotencyKey) {
        return payment(send(FieldOrders.PAYMENT_VOID, invoiceInput(invoiceId), idempotencyKey, null));
    }

    @Override
    public RefundResult refund(long invoiceId, String amount) {
        return refund(invoiceId, amount, null);
    }

    @Override
    public RefundResult refund(long invoiceId, String amount, String idempotencyKey) {
        Map<String, Object> input = Map.<String, Object>of("invoice_id", invoiceId, "amount", amount);
        JsonElement data = send(FieldOrders.PAYMENT_REFUND, input, idempotencyKey, null);
        return new RefundResult(
                Json.int64(data, "invoice_id"),
                Json.str(data, "paid_status"),
                Json.strOrNull(data, "auth_code"),
                Json.decimalOrNull(data, "refund_amount"));
    }

    @Override
    public PaymentResult settle(long invoiceId, String amount) {
        return settle(invoiceId, amount, null);
    }

    @Override
    public PaymentResult settle(long invoiceId, String amount, String idempotencyKey) {
        Map<String, Object> input = Map.<String, Object>of("invoice_id", invoiceId, "amount", amount);
        return payment(send(FieldOrders.PAYMENT_SETTLE, input, idempotencyKey, null));
    }

    @Override
    public PaymentResult reverseAuthorization(long invoiceId) {
        return reverseAuthorization(invoiceId, null);
    }

    @Override
    public PaymentResult reverseAuthorization(long invoiceId, String idempotencyKey) {
        return payment(send(FieldOrders.PAYMENT_REVERSE_AUTHORIZATION, invoiceInput(invoiceId), idempotencyKey, null));
    }

    @Override
    public PaymentResult checkStatus(long invoiceId) {
        return payment(send(FieldOrders.PAYMENT_CHECK_STATUS, invoiceInput(invoiceId), null, Boolean.TRUE));
    }

    private JsonElement send(EndpointSpec spec, Map<String, Object> input, String idempotencyKey, Boolean replaySafe) {
        Map<String, String> body = SignedBody.build(spec, input, config.publicToken(), config.hashToken());
        return RequestExecutor.execute(config, RequestOptions.post(spec.path(), body, idempotencyKey, replaySafe));
    }

    private static Map<String, Object> invoiceInput(long invoiceId) {
        return Map.<String, Object>of("invoice_id", invoiceId);
    }

    private static PaymentResult payment(JsonElement data) {
        return new PaymentResult(
                Json.int64(data, "invoice_id"),
                Json.str(data, "paid_status"),
                Json.strOrNull(data, "auth_code"));
    }
}
