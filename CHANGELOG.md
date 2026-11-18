# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and
this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - 2026-11-18

### Fixed

- `webhooks().verify()` now accepts `card_token.created` and `card_token.failed`. Both carry the
  tokenisation and the card it produced, and the server signs all of those fields; this SDK signed
  none of them, so every card-token webhook failed verification. Both events are on by default for
  every integration, so any integrator saving cards was receiving webhooks it could not verify.

### Added

- `CARD_TOKEN_CREATED` and `CARD_TOKEN_FAILED` on `WebhookEventType`.
- The saved card on `WebhookEvent`: `tokenizationId()`, `cardToken()`, `cardBrand()`, `cardLast4()`,
  `cardExpMonth()` and `cardExpYear()`. The record keeps a constructor with its previous shape, so
  code that builds an event positionally still compiles.

## [0.2.0]

### Added

- Optional `iframe` builder on `CreateInvoiceParams` (`.iframe(true)`) for embedded
  checkout. Like `payment_mode`, it is sent in the request body but excluded from the
  HMAC signature.

## [0.1.0]

### Added

- Initial release: checkouts (`invoices().create`), payment operations
  (`payments().voidInvoice` / `refund` / `settle` / `reverseAuthorization` /
  `checkStatus`), server-to-server card charges (`vcc().charge`), card tokenization
  (`cards().tokenize` / `charge` / `revoke`), recurring mandates (`recurring().create` /
  `status` / `cancel` / `pause` / `resume`), and webhook signature verification
  (`webhooks().verify` / `isValid`).
- Order-sensitive HMAC-SHA256 request signing, proven byte-identical to the PHP server and
  the JS/Python/.NET SDKs by a shared golden-signature fixture.
- **`paylink-spring-boot-starter`** — auto-configures a `PaylinkClient` bean from
  `paylink.*` properties for one-line dependency injection.
- Retry with exponential backoff and full jitter for transient failures (429, 5xx,
  connection errors, timeouts), honoring `Retry-After`. Only replay-safe requests are
  retried; charges are never replayed.
- Typed exception hierarchy (`PaylinkApiException` with `isIdempotencyConflict()` /
  `isRateLimited()` / `isForbidden()` and `retryAfter()`).
- Core has a single dependency (Gson); HTTP over the JDK `HttpClient` with a pluggable
  `Transport`. Requires Java 17.
