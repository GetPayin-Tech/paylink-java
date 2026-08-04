package com.getpayin.paylink.resources;

import com.getpayin.paylink.http.RequestExecutor;
import com.getpayin.paylink.http.RequestOptions;
import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.CardInfo;
import com.getpayin.paylink.model.ChargeCardParams;
import com.getpayin.paylink.model.ChargeTokenResult;
import com.getpayin.paylink.model.RevokeTokenResult;
import com.getpayin.paylink.model.TokenizeCardParams;
import com.getpayin.paylink.model.TokenizeCardResult;
import com.google.gson.JsonElement;
import java.util.Map;

/** Default {@link Cards} implementation. */
public final class DefaultCards implements Cards {

    private final PaylinkConfig config;

    public DefaultCards(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public TokenizeCardResult tokenize(TokenizeCardParams params) {
        return tokenize(params, null);
    }

    @Override
    public TokenizeCardResult tokenize(TokenizeCardParams params, String idempotencyKey) {
        Map<String, String> body = SignedBody.build(
                FieldOrders.CARD_TOKENIZE, params.fields(), config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.CARD_TOKENIZE.path(), body, idempotencyKey, null));

        JsonElement card = Json.child(data, "card");

        return new TokenizeCardResult(
                Json.str(data, "token"),
                new CardInfo(
                        Json.strOrNull(card, "brand"),
                        Json.strOrNull(card, "last4"),
                        toInt(Json.int64OrNull(card, "exp_month")),
                        toInt(Json.int64OrNull(card, "exp_year"))),
                Json.str(data, "status"));
    }

    @Override
    public ChargeTokenResult charge(ChargeCardParams params) {
        return charge(params, null);
    }

    @Override
    public ChargeTokenResult charge(ChargeCardParams params, String idempotencyKey) {
        Map<String, String> body = SignedBody.build(
                FieldOrders.CARD_CHARGE, params.fields(), config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.CARD_CHARGE.path(), body, idempotencyKey, null));

        return new ChargeTokenResult(
                Json.int64(data, "invoice_id"),
                Json.str(data, "invoice_number"),
                Json.decimal(data, "amount"),
                Json.str(data, "currency"),
                Json.str(data, "paid_status"),
                Json.str(data, "card_token"));
    }

    @Override
    public RevokeTokenResult revoke(String cardToken) {
        Map<String, Object> input = Map.<String, Object>of("card_token", cardToken);
        Map<String, String> body = SignedBody.build(
                FieldOrders.CARD_REVOKE, input, config.publicToken(), config.hashToken());

        JsonElement data = RequestExecutor.execute(
                config, RequestOptions.post(FieldOrders.CARD_REVOKE.path(), body, null, null));

        String message = Json.strOrNull(data, "message");
        return new RevokeTokenResult(message != null ? message : "Token revoked.");
    }

    private static Integer toInt(Long value) {
        return value != null ? value.intValue() : null;
    }
}
