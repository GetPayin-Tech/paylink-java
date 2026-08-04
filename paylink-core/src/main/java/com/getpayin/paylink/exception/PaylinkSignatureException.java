package com.getpayin.paylink.exception;

/** Thrown by {@code Webhooks.verify} when a webhook signature does not match. */
public class PaylinkSignatureException extends PaylinkException {

    public PaylinkSignatureException() {
        super("Webhook signature verification failed.");
    }

    public PaylinkSignatureException(String message) {
        super(message);
    }
}
