package com.getpayin.paylink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.getpayin.paylink.exception.PaylinkException;
import com.getpayin.paylink.exception.PaylinkSignatureException;
import com.getpayin.paylink.internal.Signature;
import com.getpayin.paylink.webhook.WebhookEvent;
import com.getpayin.paylink.webhook.WebhookEventType;
import com.getpayin.paylink.webhook.Webhooks;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebhookTest {

    private static final String HASH = "test_hash_token_abc123";

    private final Webhooks webhooks = PaylinkClient.builder().publicToken("p").hashToken(HASH).build().webhooks();

    @Test
    void verifiesValidInvoicePaid() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "invoice.paid");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 123);
        payload.addProperty("invoice_status", "PAID");
        payload.add("message", com.google.gson.JsonNull.INSTANCE);
        sign(payload, HASH);

        WebhookEvent event = webhooks.verify(payload.toString());
        assertEquals("invoice.paid", event.event());
        assertTrue(event.success());
        assertEquals(123L, event.invoiceId());
        assertNull(event.message());
    }

    @Test
    void verifiesSubscriptionOptionalFields() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "subscription.charged");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 555);
        payload.addProperty("invoice_status", "PAID");
        payload.addProperty("message", "ok");
        payload.addProperty("auth_code", "A1");
        payload.addProperty("mandate_id", "M-1");
        payload.addProperty("external_reference", "sub_1");
        payload.addProperty("subscription_status", "active");
        sign(payload, HASH);

        WebhookEvent event = webhooks.verify(payload.toString());
        assertEquals("M-1", event.mandateId());
        assertEquals("active", event.subscriptionStatus());
        assertEquals("A1", event.authCode());
    }

    @Test
    void rejectsTamperedPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "invoice.paid");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 1);
        payload.addProperty("invoice_status", "PAID");
        payload.addProperty("message", "ok");
        sign(payload, HASH);
        payload.addProperty("invoice_status", "UNPAID");

        assertFalse(webhooks.isValid(payload.toString()));
        assertThrows(PaylinkSignatureException.class, () -> webhooks.verify(payload.toString()));
    }

    @Test
    void rejectsMissingSignature() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "invoice.paid");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 1);
        payload.addProperty("invoice_status", "PAID");

        assertThrows(PaylinkSignatureException.class, () -> webhooks.verify(payload.toString()));
    }

    @Test
    void honorsHashTokenOverride() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "invoice.paid");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 9);
        payload.addProperty("invoice_status", "PAID");
        payload.addProperty("message", "ok");
        sign(payload, "other_secret");

        assertFalse(webhooks.isValid(payload.toString()));
        assertTrue(webhooks.isValid(payload.toString(), "other_secret"));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(PaylinkException.class, () -> webhooks.verify("{not json"));
    }

    /**
     * Signs a payload the way the server signs one: the always-signed four, then whichever
     * optional block the payload carries, each gated on its own leading field so that
     * {@code external_reference} is signed at the position the block that introduced it gives it.
     */
    @Test
    void verifiesCardTokenCreatedAndExposesTheSavedCard() {
        JsonObject payload = cardTokenCreated();
        sign(payload, HASH);

        WebhookEvent event = webhooks.verify(payload.toString());
        assertEquals(WebhookEventType.CARD_TOKEN_CREATED, event.event());
        assertEquals("TKZ-4f21", event.tokenizationId());
        assertEquals("CRD-9a07", event.cardToken());
        assertEquals("VISA", event.cardBrand());
        assertEquals("4242", event.cardLast4());
        assertEquals("12", event.cardExpMonth());
        assertEquals("2029", event.cardExpYear());
        assertEquals("cust-42", event.externalReference());
    }

    @Test
    void verifiesCardTokenFailedWhichCarriesNoCard() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "card_token.failed");
        payload.addProperty("success", 0);
        payload.addProperty("invoice_id", 7782);
        payload.addProperty("invoice_status", "UNPAID");
        payload.addProperty("message", "The card could not be stored for future payments.");
        payload.addProperty("tokenization_id", "TKZ-4f22");
        payload.addProperty("external_reference", "cust-43");
        sign(payload, HASH);

        WebhookEvent event = webhooks.verify(payload.toString());
        assertEquals(WebhookEventType.CARD_TOKEN_FAILED, event.event());
        assertFalse(event.success());
        assertEquals("TKZ-4f22", event.tokenizationId());
        assertNull(event.cardToken());
    }

    @Test
    void rejectsATamperedCard() {
        JsonObject payload = cardTokenCreated();
        sign(payload, HASH);
        payload.addProperty("card_last4", "1111");

        assertFalse(webhooks.isValid(payload.toString()));
    }

    private static void sign(JsonObject payload, String hashToken) {
        List<String> keys = new ArrayList<>(List.of("success", "invoice_id", "invoice_status", "message"));
        List<List<String>> blocks = List.of(
                List.of("mandate_id", "external_reference", "subscription_status"),
                List.of("tokenization_id", "external_reference", "card_token", "card_brand",
                        "card_last4", "card_exp_month", "card_exp_year"));

        for (List<String> block : blocks) {
            if (!payload.has(block.get(0))) {
                continue;
            }
            for (String key : block) {
                if (payload.has(key) && !keys.contains(key)) {
                    keys.add(key);
                }
            }
        }

        List<String> values = new ArrayList<>();
        for (String key : keys) {
            values.add(wire(payload, key));
        }
        payload.addProperty("signature", Signature.build(values, hashToken));
    }

    private static JsonObject cardTokenCreated() {
        JsonObject payload = new JsonObject();
        payload.addProperty("event", "card_token.created");
        payload.addProperty("success", 1);
        payload.addProperty("invoice_id", 7781);
        payload.addProperty("invoice_status", "REVERSED");
        payload.addProperty("message", "Card stored for future payments.");
        payload.addProperty("tokenization_id", "TKZ-4f21");
        payload.addProperty("external_reference", "cust-42");
        payload.addProperty("card_token", "CRD-9a07");
        payload.addProperty("card_brand", "VISA");
        payload.addProperty("card_last4", "4242");
        payload.addProperty("card_exp_month", "12");
        payload.addProperty("card_exp_year", "2029");
        return payload;
    }

    private static String wire(JsonObject payload, String name) {
        if (!payload.has(name) || payload.get(name).isJsonNull()) {
            return "";
        }
        return payload.get(name).getAsString();
    }
}
