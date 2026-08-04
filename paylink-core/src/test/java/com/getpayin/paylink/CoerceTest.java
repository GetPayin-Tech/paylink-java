package com.getpayin.paylink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.getpayin.paylink.exception.PaylinkException;
import com.getpayin.paylink.internal.Coerce;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoerceTest {

    @Test
    void nullBecomesEmptyString() {
        assertEquals("", Coerce.toWire(null));
    }

    @Test
    void booleanBecomesOneOrZero() {
        assertEquals("1", Coerce.toWire(true));
        assertEquals("0", Coerce.toWire(false));
    }

    @Test
    void stringPassesThrough() {
        assertEquals("250.00", Coerce.toWire("250.00"));
    }

    @Test
    void integersRenderWithoutGrouping() {
        assertEquals("250", Coerce.toWire(250));
        assertEquals("250", Coerce.toWire(250L));
    }

    @Test
    void integerValuedDoubleHasNoTrailingZero() {
        assertEquals("250", Coerce.toWire(250.0));
    }

    @Test
    void fractionalDouble() {
        assertEquals("250.5", Coerce.toWire(250.5));
    }

    @Test
    void nonFiniteThrows() {
        assertThrows(PaylinkException.class, () -> Coerce.toWire(Double.POSITIVE_INFINITY));
    }

    @Test
    void unsupportedTypeThrows() {
        assertThrows(PaylinkException.class, () -> Coerce.toWire(List.of("a")));
    }
}
