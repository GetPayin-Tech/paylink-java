package com.getpayin.paylink.exception;

/**
 * Thrown when the client is built with missing or invalid configuration (for
 * example an empty public/hash token, or a non-positive timeout).
 */
public class PaylinkConfigurationException extends PaylinkException {

    public PaylinkConfigurationException(String message) {
        super(message);
    }
}
