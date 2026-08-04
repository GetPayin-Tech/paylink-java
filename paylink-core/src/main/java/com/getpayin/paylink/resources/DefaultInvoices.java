package com.getpayin.paylink.resources;

import com.getpayin.paylink.http.RequestExecutor;
import com.getpayin.paylink.http.RequestOptions;
import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.CreateInvoiceParams;
import com.getpayin.paylink.model.CreateInvoiceResult;
import com.google.gson.JsonElement;
import java.util.Map;

/** Default {@link Invoices} implementation. */
public final class DefaultInvoices implements Invoices {

    private final PaylinkConfig config;

    public DefaultInvoices(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public CreateInvoiceResult create(CreateInvoiceParams params) {
        return create(params, null);
    }

    @Override
    public CreateInvoiceResult create(CreateInvoiceParams params, String idempotencyKey) {
        Map<String, String> body = SignedBody.build(
                FieldOrders.INVOICE_CREATE, params.fields(), config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.INVOICE_CREATE.path(), body, idempotencyKey, null));

        return new CreateInvoiceResult(
                Json.str(data, "checkout_url"),
                Json.int64(data, "invoice_id"),
                Json.str(data, "expires_at"));
    }
}
