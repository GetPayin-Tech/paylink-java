package com.getpayin.paylink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.getpayin.paylink.internal.FieldOrders;
import com.getpayin.paylink.internal.SignedBody;
import com.getpayin.paylink.model.CreateInvoiceParams;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The optional {@code iframe} field is sent in the body (coerced to {@code "1"}/{@code "0"})
 * but excluded from the HMAC signature, exactly like {@code payment_mode}.
 */
class IframeOptionTest {

    private static final String PUBLIC_TOKEN = "pub_token";
    private static final String HASH_TOKEN = "test_hash_token_abc123";

    private static CreateInvoiceParams baseParams() {
        return new CreateInvoiceParams()
                .firstName("John").lastName("Doe").email("john@example.com")
                .orderTitle("Gold Plan").orderAmount("250.00").currency("USD");
    }

    @Test
    void iframeTrueSerializesToOneInBody() {
        Map<String, String> body = SignedBody.build(
                FieldOrders.INVOICE_CREATE, baseParams().iframe(true).fields(), PUBLIC_TOKEN, HASH_TOKEN);

        assertEquals("1", body.get("iframe"));
    }

    @Test
    void iframeFalseSerializesToZeroInBody() {
        Map<String, String> body = SignedBody.build(
                FieldOrders.INVOICE_CREATE, baseParams().iframe(false).fields(), PUBLIC_TOKEN, HASH_TOKEN);

        assertEquals("0", body.get("iframe"));
    }

    @Test
    void iframeIsUnsignedSoItDoesNotChangeTheSignature() {
        Map<String, String> without = SignedBody.build(
                FieldOrders.INVOICE_CREATE, baseParams().fields(), PUBLIC_TOKEN, HASH_TOKEN);
        Map<String, String> with = SignedBody.build(
                FieldOrders.INVOICE_CREATE, baseParams().iframe(true).fields(), PUBLIC_TOKEN, HASH_TOKEN);

        // iframe is present in the body but must not affect the HMAC signature.
        assertEquals("1", with.get("iframe"));
        assertEquals(without.get("signature"), with.get("signature"));
    }
}
