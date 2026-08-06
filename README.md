# @getpayin-tech/paylink-java

[![CI](https://github.com/GetPayin-Tech/paylink-java/actions/workflows/ci.yml/badge.svg)](https://github.com/GetPayin-Tech/paylink-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.getpayin.paylink/paylink-core.svg)](https://central.sonatype.com/artifact/com.getpayin.paylink/paylink-core)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Official **server-side** Java SDK for the PayLink payment integration API. It wraps
every integration endpoint with an idiomatic, typed API and computes the
order-sensitive HMAC-SHA256 signatures for you, so you never have to build them by hand.
Ships a **Spring Boot starter** for one-line dependency injection.

- Checkouts (`invoices().create`)
- Payment operations (`payments().voidInvoice` / `refund` / `settle` / `reverseAuthorization` / `checkStatus`)
- Server-to-server card charges (`vcc().charge`)
- Card tokenization (`cards().tokenize` / `charge` / `revoke`)
- Recurring mandates (`recurring().create` / `status` / `cancel` / `pause` / `resume`)
- Webhook signature verification (`webhooks().verify`)

> **Server-side only.** Signing uses your secret `hashToken`. Never ship it to a
> browser, mobile, or desktop client.

## Requirements

- Java **17+**
- The core `paylink-core` has one dependency (Gson); the Spring Boot starter adds Spring.

## Install

**Maven** — core:

```xml
<dependency>
  <groupId>com.getpayin.paylink</groupId>
  <artifactId>paylink-core</artifactId>
  <version>0.2.0</version>
</dependency>
```

Spring Boot apps — use the starter instead (it pulls in the core):

```xml
<dependency>
  <groupId>com.getpayin.paylink</groupId>
  <artifactId>paylink-spring-boot-starter</artifactId>
  <version>0.2.0</version>
</dependency>
```

**Gradle**:

```groovy
implementation 'com.getpayin.paylink:paylink-core:0.2.0'
// or, for Spring Boot:
implementation 'com.getpayin.paylink:paylink-spring-boot-starter:0.2.0'
```

## Quick start

```java
import com.getpayin.paylink.PaylinkClient;
import com.getpayin.paylink.model.CreateInvoiceParams;
import com.getpayin.paylink.model.CreateInvoiceResult;

PaylinkClient paylink = PaylinkClient.builder()
        .publicToken(System.getenv("PAYLINK_PUBLIC_TOKEN"))
        .hashToken(System.getenv("PAYLINK_HASH_TOKEN")) // secret — server-side only
        .build();

CreateInvoiceResult checkout = paylink.invoices().create(
        new CreateInvoiceParams()
                .firstName("John").lastName("Doe").email("john@example.com")
                .orderTitle("Gold Plan")
                .orderAmount("250.00") // pass money as strings for an exact wire form
                .currency("USD"));

// Redirect the payer to the hosted checkout:
response.sendRedirect(checkout.checkoutUrl());
```

For an **embedded checkout**, pass `.iframe(true)` and render `checkout.checkoutUrl()`
inside an `<iframe>` instead of redirecting:

```java
CreateInvoiceResult checkout = paylink.invoices().create(
        new CreateInvoiceParams()
                .firstName("John").lastName("Doe").email("john@example.com")
                .orderTitle("Gold Plan").orderAmount("250.00").currency("USD")
                .iframe(true));
```

Then embed the returned checkout URL on your page. The `<iframe>` needs `allow="payment *"` so Apple Pay and Google Pay work inside the frame, and your page must listen for the completion message — in iframe mode the checkout signals the parent via `postMessage` instead of redirecting:

```html
<iframe src="CHECKOUT_URL" allow="payment *"
        style="width:100%;min-height:640px;border:0" title="Secure checkout"></iframe>
<script>
  addEventListener('message', function (e) {
    if (e.origin !== 'https://pay.getpayin.com') return; // your API base origin
    if (!e.data || e.data.type !== 'paylink_payment') return;
    window.location.href = e.data.success ? '/thank-you' : '/checkout?failed=1';
  });
</script>
```

> The embedding page's origin must exactly match your integration's registered **Origin**, or the browser blocks framing and the message never arrives.

Both credentials are issued in the PayLink dashboard under **Settings → Payment
Integrations**. `publicToken` is sent on every request; `hashToken` is the secret used
only to sign — it never leaves your server.

## Spring Boot (dependency injection)

With the starter on the classpath, set the credentials and **inject `PaylinkClient`** —
constructor injection is the idiomatic way:

```properties
paylink.public-token=pub_...
paylink.hash-token=secret_...
# optional: paylink.base-url, paylink.timeout=30s, paylink.max-retries=2
```

```java
import com.getpayin.paylink.PaylinkClient;
import com.getpayin.paylink.model.CreateInvoiceParams;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final PaylinkClient paylink;

    public CheckoutService(PaylinkClient paylink) {
        this.paylink = paylink;
    }

    public String startCheckout() {
        return paylink.invoices().create(
                new CreateInvoiceParams()
                        .firstName("John").lastName("Doe").email("john@example.com")
                        .orderTitle("Gold Plan").orderAmount("250.00").currency("USD"))
                .checkoutUrl();
    }
}
```

The auto-configured bean backs off if you define your own `PaylinkClient`, and it uses a
`Transport` bean if you provide one (for example one backed by Spring's HTTP client).

## Payment operations

```java
paylink.payments().voidInvoice(12345);
paylink.payments().settle(12345, "50.00");
paylink.payments().reverseAuthorization(12345);

PaymentResult status = paylink.payments().checkStatus(12345);
// PaymentResult[invoiceId=12345, paidStatus=..., authCode=...]

// Refunds are idempotent when you pass an idempotency key — safe to retry:
RefundResult refund = paylink.payments().refund(12345, "10.50", "refund-order-1234");
```

## Card tokenization

```java
TokenizeCardResult vaulted = paylink.cards().tokenize(
        new TokenizeCardParams()
                .firstName("Jane").lastName("Doe")
                .cardNumber("4111111111111111").cardExpiryMonth("12").cardExpiryYear("2030").cardCvv("123")
                .country("EG").address("1 Main St").city("Cairo"));

paylink.cards().charge(
        new ChargeCardParams()
                .cardToken(vaulted.token()).initiator("merchant")
                .firstName("Jane").lastName("Doe")
                .currency("USD").price("100.00").product("Monthly rebill")
                .country("EG").address("1 Main St").city("Cairo"));

paylink.cards().revoke(vaulted.token());
```

For `US` and `CA` billing addresses, also pass the state fields the API requires:
`usState` + `postalCode` (US) or `canadaState` + `postalCode` (CA).

## Recurring mandates

```java
CreateRecurringResult mandate = paylink.recurring().create(
        new CreateRecurringParams()
                .firstName("Sam").lastName("Doe").email("sam@example.com")
                .orderTitle("Gold subscription").orderAmount("250.00").currency("USD")
                .cadenceInterval("month").cadenceCount(1).totalCycles(12)
                .consentText("I authorise recurring monthly charges."),
        "sub-signup-42");

paylink.recurring().status(mandate.mandateId());
paylink.recurring().pause(mandate.mandateId());
paylink.recurring().resume(mandate.mandateId());
paylink.recurring().cancel(mandate.mandateId());
```

## Idempotency

Pass an idempotency key to make a retried write safe — the SDK sends it as the
`Idempotency-Key` header and the server returns the original result instead of charging,
refunding, or creating a second time. Keys are scoped per integration and capped at 64
characters. Honored on `invoices().create`, `vcc().charge`, `cards().charge`,
`payments().refund`, and `recurring().create`.

Reusing a key with a _different_ request is rejected as a conflict: a
`PaylinkApiException` whose `isIdempotencyConflict()` is true (HTTP 409).

## Verifying webhooks

Pass the raw request body to `verify()`. It recomputes the signature with your
`hashToken` and compares in constant time.

```java
import com.getpayin.paylink.exception.PaylinkSignatureException;
import com.getpayin.paylink.webhook.WebhookEvent;
import com.getpayin.paylink.webhook.WebhookEventType;

try {
    WebhookEvent event = paylink.webhooks().verify(rawRequestBody);
    if (WebhookEventType.INVOICE_PAID.equals(event.event())) {
        // fulfil the order
    }
} catch (PaylinkSignatureException e) {
    // reject — do not trust this payload
}
```

> PayLink webhook signatures carry no timestamp, so verification does not protect against
> replay. Pair it with your own idempotency keyed on `invoice_id`.

## Error handling

Every failure extends `PaylinkException`:

| Exception                      | When                                                                                                                    |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------- |
| `PaylinkConfigurationException`| Invalid client configuration (missing tokens, non-positive timeout).                                                   |
| `PaylinkApiException`          | The API returned an error. Carries `status()`, `errors()`, `raw()`, `retryAfter()`, and the `isIdempotencyConflict()` / `isRateLimited()` / `isForbidden()` flags. |
| `PaylinkSignatureException`    | A webhook signature did not verify.                                                                                     |
| `PaylinkConnectionException`   | Network failure or timeout (no HTTP response).                                                                          |

## Retries and rate limiting

Every integration endpoint is rate limited server-side, so 429s are an expected condition
under burst traffic. The SDK retries transient failures — **429, 5xx, connection errors,
and timeouts** — with exponential backoff and full jitter, honoring the server's
`Retry-After` header when present.

**A request is only ever replayed when replaying it cannot double-charge:** GETs, any call
you pass an idempotency key to, and pure reads such as `payments().checkStatus`. A bare
`vcc().charge` or `cards().charge` is never replayed. Tune with `.maxRetries(...)` (0
disables). The timeout applies to each attempt.

## Amounts and precision

Signatures are computed over the exact bytes sent on the wire. To avoid any
floating-point ambiguity, **pass monetary amounts as strings** (e.g. `"10.50"`).

## Custom transport

Implement `com.getpayin.paylink.http.Transport` for a proxy-aware or pooled client, a
framework's HTTP client, or a mock in tests, and set it with `.transport(...)` (or expose
it as a Spring bean). The default uses the JDK's `HttpClient`.

## API reference

The full HTTP API — endpoints, fields, error codes, and test cards — is documented in the
PayLink API reference: <https://pay.getpayin.com/docs/payment_integration/index.html>

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) — in particular the note on signed-field ordering,
which must stay in lockstep with the server.

Security issues: see [SECURITY.md](SECURITY.md). Please do not open a public issue for a
vulnerability.

## License

MIT
