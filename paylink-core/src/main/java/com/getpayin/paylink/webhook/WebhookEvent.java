package com.getpayin.paylink.webhook;

import java.util.Map;

/**
 * A verified webhook event; {@code raw} holds the full decoded body.
 *
 * <p>The card components are filled only on {@code card_token.created}, and only once the card was
 * actually vaulted: a {@code card_token.failed} event carries the tokenisation and nothing else.
 */
public record WebhookEvent(
        String event,
        boolean success,
        Map<String, Object> raw,
        Long invoiceId,
        String invoiceStatus,
        String message,
        String authCode,
        String mandateId,
        String externalReference,
        String subscriptionStatus,
        String eventTriggeredAt,
        String timezone,
        String tokenizationId,
        String cardToken,
        String cardBrand,
        String cardLast4,
        String cardExpMonth,
        String cardExpYear) {

    /**
     * The shape this record had before it carried a card, so code that constructs an event
     * positionally still compiles.
     */
    public WebhookEvent(
            String event,
            boolean success,
            Map<String, Object> raw,
            Long invoiceId,
            String invoiceStatus,
            String message,
            String authCode,
            String mandateId,
            String externalReference,
            String subscriptionStatus,
            String eventTriggeredAt,
            String timezone) {
        this(event, success, raw, invoiceId, invoiceStatus, message, authCode, mandateId,
                externalReference, subscriptionStatus, eventTriggeredAt, timezone,
                null, null, null, null, null, null);
    }
}
