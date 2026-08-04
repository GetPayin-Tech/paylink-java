package com.getpayin.paylink;

import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.resources.Cards;
import com.getpayin.paylink.resources.DefaultCards;
import com.getpayin.paylink.resources.DefaultInvoices;
import com.getpayin.paylink.resources.DefaultPayments;
import com.getpayin.paylink.resources.DefaultRecurring;
import com.getpayin.paylink.resources.DefaultVcc;
import com.getpayin.paylink.resources.Invoices;
import com.getpayin.paylink.resources.Payments;
import com.getpayin.paylink.resources.Recurring;
import com.getpayin.paylink.resources.Vcc;
import com.getpayin.paylink.webhook.DefaultWebhooks;
import com.getpayin.paylink.webhook.Webhooks;

/** Default {@link PaylinkClient} implementation, wiring the services from a resolved config. */
public final class DefaultPaylinkClient implements PaylinkClient {

    private final String publicToken;
    private final String baseUrl;
    private final Invoices invoices;
    private final Payments payments;
    private final Vcc vcc;
    private final Cards cards;
    private final Recurring recurring;
    private final Webhooks webhooks;

    public DefaultPaylinkClient(PaylinkConfig config) {
        this.publicToken = config.publicToken();
        this.baseUrl = config.baseUrl();
        this.invoices = new DefaultInvoices(config);
        this.payments = new DefaultPayments(config);
        this.vcc = new DefaultVcc(config);
        this.cards = new DefaultCards(config);
        this.recurring = new DefaultRecurring(config);
        this.webhooks = new DefaultWebhooks(config);
    }

    @Override
    public Invoices invoices() {
        return invoices;
    }

    @Override
    public Payments payments() {
        return payments;
    }

    @Override
    public Vcc vcc() {
        return vcc;
    }

    @Override
    public Cards cards() {
        return cards;
    }

    @Override
    public Recurring recurring() {
        return recurring;
    }

    @Override
    public Webhooks webhooks() {
        return webhooks;
    }

    @Override
    public String toString() {
        return "PaylinkClient{publicToken=" + publicToken + ", baseUrl=" + baseUrl + "}";
    }
}
