package com.getpayin.paylink.model;

/** The result of creating a recurring mandate. */
public record CreateRecurringResult(String checkoutUrl, String mandateId, long invoiceId, String expiresAt) {
}
