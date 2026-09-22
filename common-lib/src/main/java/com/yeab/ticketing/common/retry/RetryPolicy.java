package com.yeab.ticketing.common.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Bounded exponential backoff with jitter for idempotent or safe-to-retry operations.
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final Duration maxDelay;
    private final double jitterFactor;

    public RetryPolicy(int maxAttempts, Duration initialDelay, Duration maxDelay, double jitterFactor) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialDelay = Objects.requireNonNull(initialDelay);
        this.maxDelay = Objects.requireNonNull(maxDelay);
        this.jitterFactor = jitterFactor;
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(5, Duration.ofMillis(200), Duration.ofSeconds(5), 0.25);
    }

    public <T> T execute(RetryableOperation<T> operation, Predicate<Exception> retryOn) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception ex) {
                last = ex;
                if (attempt == maxAttempts || !retryOn.test(ex)) {
                    throw ex;
                }
                sleep(backoffForAttempt(attempt));
            }
        }
        throw last;
    }

    Duration backoffForAttempt(int attempt) {
        long baseMillis = initialDelay.multipliedBy(1L << Math.min(attempt - 1, 10)).toMillis();
        long capped = Math.min(baseMillis, maxDelay.toMillis());
        long jitter = (long) (capped * jitterFactor * ThreadLocalRandom.current().nextDouble());
        return Duration.ofMillis(capped + jitter);
    }

    private static void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws Exception;
    }
}
