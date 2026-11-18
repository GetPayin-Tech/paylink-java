package com.getpayin.paylink.webhook;

/** Every webhook event type PayLink can deliver. */
public final class WebhookEventType {

    public static final String INVOICE_PAID = "invoice.paid";
    public static final String INVOICE_NOT_PAID = "invoice.not_paid";
    public static final String INVOICE_VOIDED = "invoice.voided";
    public static final String INVOICE_REFUNDED = "invoice.refunded";
    public static final String INVOICE_AUTHORIZATION_REVERSED = "invoice.authorization_reversed";
    public static final String INVOICE_AUTHORIZED = "invoice.authorized";
    public static final String SUBSCRIPTION_ACTIVATED = "subscription.activated";
    public static final String SUBSCRIPTION_CHARGED = "subscription.charged";
    public static final String SUBSCRIPTION_PAYMENT_FAILED = "subscription.payment_failed";
    public static final String SUBSCRIPTION_SUSPENDED = "subscription.suspended";
    public static final String SUBSCRIPTION_CANCELLED = "subscription.cancelled";
    public static final String SUBSCRIPTION_COMPLETED = "subscription.completed";
    public static final String INSTALLMENT_PLAN_ACTIVATED = "installment.plan_activated";
    public static final String INSTALLMENT_CHARGED = "installment.charged";
    public static final String INSTALLMENT_PAYMENT_FAILED = "installment.payment_failed";
    public static final String INSTALLMENT_PLAN_SUSPENDED = "installment.plan_suspended";
    public static final String INSTALLMENT_PLAN_COMPLETED = "installment.plan_completed";
    public static final String VCC_PAYMENT_SUCCESS = "vcc.payment_success";
    public static final String VCC_PAYMENT_FAILED = "vcc.payment_failed";
    public static final String CARD_TOKEN_CHARGE_SUCCEEDED = "card_token.charge_succeeded";
    public static final String CARD_TOKEN_CHARGE_FAILED = "card_token.charge_failed";
    public static final String CARD_TOKEN_CREATED = "card_token.created";
    public static final String CARD_TOKEN_FAILED = "card_token.failed";

    private WebhookEventType() {
    }
}
