package com.getpayin.paylink.resources;

import com.getpayin.paylink.http.RequestExecutor;
import com.getpayin.paylink.http.RequestOptions;
import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.VccChargeParams;
import com.getpayin.paylink.model.VccChargeResult;
import com.google.gson.JsonElement;
import java.util.Map;

/** Default {@link Vcc} implementation. */
public final class DefaultVcc implements Vcc {

    private final PaylinkConfig config;

    public DefaultVcc(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public VccChargeResult charge(VccChargeParams params) {
        return charge(params, null);
    }

    @Override
    public VccChargeResult charge(VccChargeParams params, String idempotencyKey) {
        Map<String, String> body = SignedBody.build(
                FieldOrders.VCC_CHARGE, params.fields(), config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.VCC_CHARGE.path(), body, idempotencyKey, null));

        return new VccChargeResult(
                Json.int64(data, "invoice_id"),
                Json.str(data, "invoice_number"),
                Json.decimal(data, "amount"),
                Json.str(data, "currency"),
                Json.str(data, "paid_status"));
    }
}
