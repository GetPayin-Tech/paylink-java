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

    private static void sign(JsonObject payload, String hashToken) {
        List<String> values = new ArrayList<>();
        values.add(wire(payload, "success"));
        values.add(wire(payload, "invoice_id"));
        values.add(wire(payload, "invoice_status"));
        values.add(wire(payload, "message"));
        for (String key : List.of("mandate_id", "external_reference", "subscription_status")) {
            if (payload.has(key)) {
                values.add(wire(payload, key));
            }
        }
        payload.addProperty("signature", Signature.build(values, hashToken));
    }

    private static String wire(JsonObject payload, String name) {
        if (!payload.has(name) || payload.get(name).isJsonNull()) {
            return "";
        }
        return payload.get(name).getAsString();
    }
}
