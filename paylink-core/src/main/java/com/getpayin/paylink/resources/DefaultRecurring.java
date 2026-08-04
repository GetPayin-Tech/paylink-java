package com.getpayin.paylink.resources;

import com.getpayin.paylink.http.RequestExecutor;
import com.getpayin.paylink.http.RequestOptions;
import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.Signature;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.CreateRecurringParams;
import com.getpayin.paylink.model.CreateRecurringResult;
import com.getpayin.paylink.model.MandateActionResult;
import com.getpayin.paylink.model.MandateStatusResult;
import com.google.gson.JsonElement;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Default {@link Recurring} implementation. */
public final class DefaultRecurring implements Recurring {

    private final PaylinkConfig config;

    public DefaultRecurring(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public CreateRecurringResult create(CreateRecurringParams params) {
        return create(params, null);
    }

    @Override
    public CreateRecurringResult create(CreateRecurringParams params, String idempotencyKey) {
        Map<String, String> body = SignedBody.build(
                FieldOrders.RECURRING_CREATE, params.fields(), config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.RECURRING_CREATE.path(), body, idempotencyKey, null));

        return new CreateRecurringResult(
                Json.str(data, "checkout_url"),
                Json.str(data, "mandate_id"),
                Json.int64(data, "invoice_id"),
                Json.str(data, "expires_at"));
    }

    @Override
    public MandateStatusResult status(String mandateId) {
        Map<String, String> query = Map.of("token", config.publicToken(), "signature", signMandate(mandateId));
        JsonElement data = RequestExecutor.execute(config, RequestOptions.get(mandatePath(mandateId), query));

        return new MandateStatusResult(
                Json.str(data, "mandate_id"),
                Json.str(data, "status"),
                Json.int64(data, "amount"),
                Json.int64(data, "completed_cycles"),
                Json.int64OrNull(data, "total_cycles"),
                Json.strOrNull(data, "next_charge_at"));
    }

    @Override
    public MandateActionResult cancel(String mandateId) {
        return action(mandateId, "cancel");
    }

    @Override
    public MandateActionResult pause(String mandateId) {
        return action(mandateId, "pause");
    }

    @Override
    public MandateActionResult resume(String mandateId) {
        return action(mandateId, "resume");
    }

    @SuppressWarnings("unchecked")
    private MandateActionResult action(String mandateId, String actionName) {
        Map<String, String> body = Map.of("token", config.publicToken(), "signature", signMandate(mandateId));
        JsonElement data = RequestExecutor.execute(
                config, new RequestOptions("POST", mandatePath(mandateId) + "/" + actionName, body, null, null, null));

        Object plain = Json.toPlain(data);
        Map<String, Object> raw = plain instanceof Map ? (Map<String, Object>) plain : Map.of();
        return new MandateActionResult(Json.strOrNull(data, "status"), raw);
    }

    private String signMandate(String mandateId) {
        return Signature.build(List.of(mandateId), config.hashToken());
    }

    private static String mandatePath(String mandateId) {
        return "/api/v2/integration/recurring/" + URLEncoder.encode(mandateId, StandardCharsets.UTF_8);
    }
}
