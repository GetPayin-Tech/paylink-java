package com.getpayin.paylink.http;

import java.util.Map;

/** The minimal response the SDK relies on. Header lookup is case-insensitive. */
public record PaylinkHttpResponse(int status, String body, Map<String, String> headers) {

    public String header(String name) {
        if (headers == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
