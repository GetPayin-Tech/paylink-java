package com.getpayin.paylink.webhook;

import java.util.Map;

/** A verified webhook event; {@code raw} holds the full decoded body. */
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
        String timezone) {
}
