package com.getpayin.paylink.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Default transport built on the JDK's {@link java.net.http.HttpClient} (no dependencies). */
public final class JdkHttpTransport implements Transport {

    private final HttpClient client;

    public JdkHttpTransport() {
        this(HttpClient.newBuilder().build());
    }

    public JdkHttpTransport(HttpClient client) {
        this.client = client;
    }

    @Override
    public PaylinkHttpResponse send(String method, String url, Map<String, String> headers, byte[] body, Duration timeout) throws Exception {
        HttpRequest.BodyPublisher publisher = body != null
                ? HttpRequest.BodyPublishers.ofByteArray(body)
                : HttpRequest.BodyPublishers.noBody();

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .method(method, publisher);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        Map<String, String> responseHeaders = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
            responseHeaders.put(entry.getKey(), String.join(",", entry.getValue()));
        }

        return new PaylinkHttpResponse(response.statusCode(), response.body(), responseHeaders);
    }
}
