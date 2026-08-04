package com.getpayin.paylink.model;

/** The result of a void / settle / reverse / check-status operation. */
public record PaymentResult(long invoiceId, String paidStatus, String authCode) {
}
