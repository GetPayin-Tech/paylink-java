package com.getpayin.paylink.model;

/** The result of creating a checkout invoice. */
public record CreateInvoiceResult(String checkoutUrl, long invoiceId, String expiresAt) {
}
