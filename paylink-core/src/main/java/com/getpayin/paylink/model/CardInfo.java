package com.getpayin.paylink.model;

/** Summary of a tokenized card. Fields are {@code null} when the API does not return them. */
public record CardInfo(String brand, String last4, Integer expMonth, Integer expYear) {
}
