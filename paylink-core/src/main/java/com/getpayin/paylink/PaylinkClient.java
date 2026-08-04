package com.getpayin.paylink;

import com.getpayin.paylink.resources.Cards;
import com.getpayin.paylink.resources.Invoices;
import com.getpayin.paylink.resources.Payments;
import com.getpayin.paylink.resources.Recurring;
import com.getpayin.paylink.resources.Vcc;
import com.getpayin.paylink.webhook.Webhooks;

/**
 * The PayLink integration client. Build one with {@link #builder()} (or inject it via
 * the Spring Boot starter), then use the service accessors. This is the abstraction to
 * depend on.
 *
 * <pre>{@code
 * PaylinkClient paylink = PaylinkClient.builder()
 *         .publicToken(System.getenv("PAYLINK_PUBLIC_TOKEN"))
 *         .hashToken(System.getenv("PAYLINK_HASH_TOKEN"))
 *         .build();
 *
 * CreateInvoiceResult checkout = paylink.invoices().create(
 *         new CreateInvoiceParams().firstName("John").lastName("Doe")
 *                 .email("john@example.com").orderTitle("Gold Plan")
 *                 .orderAmount("250.00").currency("USD"));
 * }</pre>
 */
public interface PaylinkClient {

    /** Create checkouts. */
    Invoices invoices();

    /** Void, refund, settle, reverse authorization, and check status. */
    Payments payments();

    /** Server-to-server raw-card charges. */
    Vcc vcc();

    /** Tokenize, charge, and revoke stored cards. */
    Cards cards();

    /** Create and manage recurring mandates. */
    Recurring recurring();

    /** Verify inbound webhook signatures. */
    Webhooks webhooks();

    /** A new builder for constructing a client. */
    static PaylinkClientBuilder builder() {
        return new PaylinkClientBuilder();
    }
}
