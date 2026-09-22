package com.yeab.ticketing.common.cache;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TtlLruCacheTest {

    @Test
    void evictsLeastRecentlyUsedWhenOverCapacity() {
        TtlLruCache<String, String> cache = new TtlLruCache<>(2, Duration.ofMinutes(5));
        cache.put("a", "1");
        cache.put("b", "2");
        cache.get("a");
        cache.put("c", "3");
        assertTrue(cache.get("b").isEmpty());
        assertEquals(Optional.of("1"), cache.get("a"));
        assertEquals(Optional.of("3"), cache.get("c"));
    }

    @Test
    void expiresEntriesAfterTtl() {
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        MutableClock clock = new MutableClock(start);
        TtlLruCache<String, String> cache = new TtlLruCache<>(10, Duration.ofSeconds(30), clock);
        cache.put("k", "v");
        assertEquals(Optional.of("v"), cache.get("k"));
        clock.advance(Duration.ofSeconds(31));
        assertTrue(cache.get("k").isEmpty());
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
