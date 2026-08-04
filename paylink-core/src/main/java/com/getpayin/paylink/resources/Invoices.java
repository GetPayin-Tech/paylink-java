package com.getpayin.paylink.resources;

import com.getpayin.paylink.model.CreateInvoiceParams;
import com.getpayin.paylink.model.CreateInvoiceResult;

/** Checkout invoices. */
public interface Invoices {

    /** Creates an invoice and returns a temporary signed checkout URL. */
    CreateInvoiceResult create(CreateInvoiceParams params);

    /** Creates an invoice; pass an {@code idempotencyKey} to make a retried create safe. */
    CreateInvoiceResult create(CreateInvoiceParams params, String idempotencyKey);
}
