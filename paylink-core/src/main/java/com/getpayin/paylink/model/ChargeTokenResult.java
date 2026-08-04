package com.getpayin.paylink.model;

import java.math.BigDecimal;

/** The result of charging a stored card token. */
public record ChargeTokenResult(long invoiceId, String invoiceNumber, BigDecimal amount, String currency, String paidStatus, String cardToken) {
}
