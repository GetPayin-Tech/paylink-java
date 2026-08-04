package com.getpayin.paylink.model;

import java.util.Map;

/** The result of a cancel / pause / resume — the new status plus the raw body. */
public record MandateActionResult(String status, Map<String, Object> raw) {
}
