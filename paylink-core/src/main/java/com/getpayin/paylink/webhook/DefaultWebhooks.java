package com.getpayin.paylink.webhook;

import com.getpayin.paylink.exception.PaylinkException;
import com.getpayin.paylink.exception.PaylinkSignatureException;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.Signature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default webhook verifier. Recomputes the signature over the concatenated values of an
 * ordered subset of the payload, exactly as the server signs it: {@code success},
 * {@code invoice_id}, {@code invoice_status}, {@code message}, then whichever of the two
 * optional blocks the payload carries — a recurring mandate's fields, or a card-token
 * event's tokenisation and card. The signature travels in the payload body.
 */
public final class DefaultWebhooks implements Webhooks {

    private static final List<String> ALWAYS_SIGNED = List.of("success", "invoice_id", "invoice_status", "message");

    /** The fields a recurring mandate adds, in the order the server adds them. */
    private static final List<String> MANDATE_SIGNED = List.of("mandate_id", "external_reference", "subscription_status");

    /**
     * The fields a card-token event adds, in the order the server adds them.
     *
     * <p>{@code card_token.created} and {@code card_token.failed} are the only events that carry a
     * tokenisation, and both are on by default for every integration. The server writes
     * {@code tokenization_id} and {@code external_reference} first, then the card's details — and
     * only when the card was actually vaulted, so a failed tokenisation signs the first two alone.
     *
     * <p>Order matters because the signature is a separator-less concatenation: on an event with no
     * mandate, {@code external_reference} is signed AFTER {@code tokenization_id}, not at the
     * position it holds on a subscription event.
     */
    private static final List<String> TOKENIZATION_SIGNED = List.of(
            "tokenization_id", "external_reference", "card_token", "card_brand", "card_last4",
            "card_exp_month", "card_exp_year");

    private final PaylinkConfig config;

    public DefaultWebhooks(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public WebhookEvent verify(String payload) {
        return verify(payload, null);
    }

    @Override
    public WebhookEvent verify(String payload, String hashToken) {
        JsonObject object = parse(payload);
        String secret = hashToken != null ? hashToken : config.hashToken();

        String provided = Json.strOrNull(object, "signature");
        if (provided == null || provided.isEmpty()) {
            throw new PaylinkSignatureException("Webhook payload is missing a signature.");
        }

        if (!Signature.signaturesEqual(computeSignature(object, secret), provided)) {
            throw new PaylinkSignatureException();
        }

        return toEvent(object);
    }

    @Override
    public boolean isValid(String payload) {
        return isValid(payload, null);
    }

    @Override
    public boolean isValid(String payload, String hashToken) {
        try {
            verify(payload, hashToken);
            return true;
        } catch (PaylinkSignatureException e) {
            return false;
        }
    }

    /**
     * Rebuilds the expected signature the way the server assembles it.
     *
     * <p>{@code PaymentIntegrationWebhookJob} appends a whole block at a time — the mandate fields
     * when the invoice belongs to one, the tokenisation fields when the event is about a card — and
     * PHP keeps a key at the position it was first written. A block is therefore gated on its own
     * leading field rather than on the individual fields inside it: {@code external_reference}
     * belongs to whichever block introduced it, and is never signed twice.
     */
    private String computeSignature(JsonObject object, String hashToken) {
        List<String> keys = new ArrayList<>(ALWAYS_SIGNED);
        for (List<String> block : List.of(MANDATE_SIGNED, TOKENIZATION_SIGNED)) {
            if (!object.has(block.get(0))) {
                continue;
            }
            for (String key : block) {
                if (object.has(key) && !keys.contains(key)) {
                    keys.add(key);
                }
            }
        }

        List<String> values = new ArrayList<>();
        for (String key : keys) {
            values.add(fieldToWire(object, key));
        }
        return Signature.build(values, hashToken);
    }

    private String fieldToWire(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return "";
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? "1" : "0";
        }
        return primitive.getAsString();
    }

    @SuppressWarnings("unchecked")
    private WebhookEvent toEvent(JsonObject object) {
        Object plain = Json.toPlain(object);
        Map<String, Object> raw = plain instanceof Map ? (Map<String, Object>) plain : Map.of();

        return new WebhookEvent(
                Json.strOrNull(object, "event"),
                "1".equals(fieldToWire(object, "success")),
                raw,
                Json.int64OrNull(object, "invoice_id"),
                Json.strOrNull(object, "invoice_status"),
                Json.strOrNull(object, "message"),
                Json.strOrNull(object, "auth_code"),
                Json.strOrNull(object, "mandate_id"),
                Json.strOrNull(object, "external_reference"),
                Json.strOrNull(object, "subscription_status"),
                Json.strOrNull(object, "event_triggered_at"),
                Json.strOrNull(object, "timezone"),
                Json.strOrNull(object, "tokenization_id"),
                Json.strOrNull(object, "card_token"),
                Json.strOrNull(object, "card_brand"),
                Json.strOrNull(object, "card_last4"),
                Json.strOrNull(object, "card_exp_month"),
                Json.strOrNull(object, "card_exp_year"));
    }

    private JsonObject parse(String payload) {
        try {
            JsonElement element = Json.parse(payload);
            if (!element.isJsonObject()) {
                throw new PaylinkException("Webhook payload must decode to a JSON object.");
            }
            return element.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new PaylinkException("Webhook payload is not valid JSON.", e);
        }
    }
}
