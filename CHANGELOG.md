# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and
this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
