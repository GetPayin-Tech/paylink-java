package com.getpayin.paylink.model;

/** The result of vaulting a card. */
public record TokenizeCardResult(String token, CardInfo card, String status) {
}
