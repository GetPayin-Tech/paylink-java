package com.getpayin.paylink.resources;

import com.getpayin.paylink.model.PaymentResult;
import com.getpayin.paylink.model.RefundResult;

/** Payment operations on an existing invoice. */
public interface Payments {

    /** Voids a paid invoice. */
    PaymentResult voidInvoice(long invoiceId);

    PaymentResult voidInvoice(long invoiceId, String idempotencyKey);

    /** Refunds a paid invoice, full or partial. Pass an {@code idempotencyKey} to make retries safe. */
    RefundResult refund(long invoiceId, String amount);

    RefundResult refund(long invoiceId, String amount, String idempotencyKey);

    /** Captures an authorized invoice. */
    PaymentResult settle(long invoiceId, String amount);

    PaymentResult settle(long invoiceId, String amount, String idempotencyKey);

    /** Reverses an authorization hold. */
    PaymentResult reverseAuthorization(long invoiceId);

    PaymentResult reverseAuthorization(long invoiceId, String idempotencyKey);

    /** Queries the current gateway status of an invoice (a pure read, so it is retried). */
    PaymentResult checkStatus(long invoiceId);
}
