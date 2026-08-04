package com.getpayin.paylink.model;

import java.math.BigDecimal;

/** The result of a refund; {@code refundAmount} is {@code null} when not reported. */
public record RefundResult(long invoiceId, String paidStatus, String authCode, BigDecimal refundAmount) {
}
