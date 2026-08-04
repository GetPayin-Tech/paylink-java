package com.getpayin.paylink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.getpayin.paylink.exception.PaylinkApiException;
import com.getpayin.paylink.exception.PaylinkConnectionException;
import com.getpayin.paylink.http.PaylinkHttpResponse;
import com.getpayin.paylink.http.Transport;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.model.CreateInvoiceParams;
import com.getpayin.paylink.model.VccChargeParams;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestExecutorTest {

    private static final String CHECKOUT = "{\"checkout_url\":\"https://x/y\",\"invoice_id\":1,\"expires_at\":\"t\"}";
    private static final String PAYMENT = "{\"invoice_id\":1,\"paid_status\":\"PAID\",\"auth_code\":\"A\"}";

    @Test
    void successEnvelopeIsUnwrapped() {
        FakeTransport transport = new FakeTransport(ok(CHECKOUT));
        var result = client(2, transport).invoices().create(invoice());
        assertEquals(1L, result.invoiceId());
        assertEquals(1, transport.calls);
    }

    @Test
    void retries429WhenIdempotencyKeyPresent() {
        FakeTransport transport = new FakeTransport(err(429, "{\"success\":false,\"message\":\"rate\"}"), ok(CHECKOUT));
        var result = client(2, transport).invoices().create(invoice(), "k");
        assertEquals(1L, result.invoiceId());
        assertEquals(2, transport.calls);
        assertEquals("k", transport.headers.get(0).get("Idempotency-Key"));
    }

    @Test
    void bareChargeIsNotRetried() {
        FakeTransport transport = new FakeTransport(err(500, "{\"success\":false,\"message\":\"boom\"}"));
        PaylinkApiException error = assertThrows(PaylinkApiException.class,
                () -> client(2, transport).vcc().charge(card()));
        assertEquals(500, error.status());
        assertEquals(1, transport.calls);
    }

    @Test
    void checkStatusIsReplaySafe() {
        FakeTransport transport = new FakeTransport(err(503, "{\"success\":false,\"message\":\"down\"}"), ok(PAYMENT));
        var result = client(2, transport).payments().checkStatus(1);
        assertEquals("PAID", result.paidStatus());
        assertEquals(2, transport.calls);
    }

    @Test
    void maxRetriesZeroDisables() {
        FakeTransport transport = new FakeTransport(err(429, "{\"success\":false,\"message\":\"rate\"}"), ok(CHECKOUT));
        assertThrows(PaylinkApiException.class, () -> client(0, transport).invoices().create(invoice(), "k"));
        assertEquals(1, transport.calls);
    }

    @Test
    void nonTransientErrorIsNotRetried() {
        FakeTransport transport = new FakeTransport(
                err(422, "{\"success\":false,\"message\":\"Invalid signature.\",\"errors\":{\"signature\":[\"bad\"]}}"));
        PaylinkApiException error = assertThrows(PaylinkApiException.class,
                () -> client(2, transport).invoices().create(invoice(), "k"));
        assertEquals(422, error.status());
        assertNotNull(error.errors());
        assertEquals(1, transport.calls);
    }

    @Test
    void connectionErrorIsWrappedAndRetried() {
        FakeTransport transport = new FakeTransport(new IOException("dns"), ok(CHECKOUT));
        var result = client(2, transport).invoices().create(invoice(), "k");
        assertEquals(1L, result.invoiceId());
        assertEquals(2, transport.calls);
    }

    @Test
    void connectionErrorSurfacesWhenExhausted() {
        FakeTransport transport = new FakeTransport(new IOException("dns"));
        assertThrows(PaylinkConnectionException.class, () -> client(0, transport).invoices().create(invoice(), "k"));
    }

    @Test
    void errorFlags() {
        assertTrue(new PaylinkApiException("x", 409, null, null, null).isIdempotencyConflict());
        assertTrue(new PaylinkApiException("x", 429, null, null, null).isRateLimited());
        assertTrue(new PaylinkApiException("x", 403, null, null, null).isForbidden());
    }

    private static PaylinkClient client(int maxRetries, Transport transport) {
        PaylinkConfig config = new PaylinkConfig("p", "s", "https://x",
                Duration.ofSeconds(30), maxRetries, transport, Duration.ofMillis(1), duration -> {
                }, () -> 0.0);
        return new DefaultPaylinkClient(config);
    }

    private static PaylinkHttpResponse ok(String data) {
        return new PaylinkHttpResponse(200, "{\"success\":true,\"data\":" + data + "}", Map.of());
    }

    private static PaylinkHttpResponse err(int status, String body) {
        return new PaylinkHttpResponse(status, body, Map.of());
    }

    private static CreateInvoiceParams invoice() {
        return new CreateInvoiceParams().firstName("J").lastName("D").email("j@e.com")
                .orderTitle("X").orderAmount("10.00").currency("USD");
    }

    private static VccChargeParams card() {
        return new VccChargeParams().firstName("S").lastName("S").currencyId(1).price("10").product("W")
                .cardNumber("4111111111111111").cardExpiryMonth("12").cardExpiryYear("2030")
                .country("EG").address("A").city("C");
    }

    private static final class FakeTransport implements Transport {

        private final Deque<Object> responses;
        private int calls;
        private final List<Map<String, String>> headers = new ArrayList<>();

        private FakeTransport(Object... items) {
            this.responses = new ArrayDeque<>(List.of(items));
        }

        @Override
        public PaylinkHttpResponse send(String method, String url, Map<String, String> requestHeaders, byte[] body, Duration timeout) throws Exception {
            calls++;
            headers.add(new LinkedHashMap<>(requestHeaders));
            Object item = responses.poll();
            if (item instanceof Exception exception) {
                throw exception;
            }
            return (PaylinkHttpResponse) item;
        }
    }
}
