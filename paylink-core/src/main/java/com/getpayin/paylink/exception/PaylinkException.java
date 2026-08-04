package com.getpayin.paylink.exception;

/**
 * Base class for every error thrown by the SDK. Catch this to handle any PayLink
 * failure generically, or narrow to a subclass for specific handling.
 */
public class PaylinkException extends RuntimeException {

    public PaylinkException(String message) {
        super(message);
    }

    public PaylinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
