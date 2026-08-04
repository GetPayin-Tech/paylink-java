package com.getpayin.paylink.model;

/** A mandate's current status. {@code amount} is in the currency's minor units. */
public record MandateStatusResult(String mandateId, String status, long amount, long completedCycles, Long totalCycles, String nextChargeAt) {
}
