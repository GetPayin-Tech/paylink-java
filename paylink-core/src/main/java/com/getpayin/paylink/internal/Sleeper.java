package com.getpayin.paylink.internal;

import java.time.Duration;

/** Injectable delay used for retry backoff (defaults to {@code Thread.sleep}). */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;
}
