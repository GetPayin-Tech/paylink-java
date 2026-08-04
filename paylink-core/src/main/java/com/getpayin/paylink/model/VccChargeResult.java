package com.getpayin.paylink.model;

import java.math.BigDecimal;

/** The result of a server-to-server card charge. */
public record VccChargeResult(long invoiceId, String invoiceNumber, BigDecimal amount, String currency, String paidStatus) {
}
