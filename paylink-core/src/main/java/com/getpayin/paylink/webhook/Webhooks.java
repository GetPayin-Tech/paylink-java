package com.getpayin.paylink.webhook;

/** Inbound webhook signature verification. */
public interface Webhooks {

    /**
     * Verifies a webhook from its raw JSON body and returns the parsed event. Throws
     * {@code PaylinkSignatureException} if the signature is missing or does not match.
     * PayLink webhook signatures carry no timestamp, so this does not protect against
     * replay — pair it with your own idempotency on {@code invoice_id}.
     */
    WebhookEvent verify(String payload);

    /** Verifies a webhook using an override {@code hashToken} (e.g. a different integration). */
    WebhookEvent verify(String payload, String hashToken);

    /** Non-throwing variant of {@link #verify(String)}. */
    boolean isValid(String payload);

    /** Non-throwing variant of {@link #verify(String, String)}. */
    boolean isValid(String payload, String hashToken);
}
