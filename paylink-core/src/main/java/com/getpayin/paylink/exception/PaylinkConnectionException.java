package com.getpayin.paylink.exception;

/**
 * Thrown when the request never produced an HTTP response — a network failure, DNS
 * error, or the configured timeout elapsing before the server replied.
 */
public class PaylinkConnectionException extends PaylinkException {

    public PaylinkConnectionException(String message) {
        super(message);
    }

    public PaylinkConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
