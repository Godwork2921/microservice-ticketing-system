package com.yeab.ticketing.common.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe LRU cache with per-entry TTL. Suitable for read-heavy metadata (events, venues),
 * not for reservation availability truth.
 */
public final class TtlLruCache<K, V> {

    private final int maxSize;
    private final Duration ttl;
    private final Clock clock;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final LinkedHashMap<K, CacheEntry<V>> map;

    public TtlLruCache(int maxSize, Duration ttl) {
        this(maxSize, ttl, Clock.systemUTC());
    }

    TtlLruCache(int maxSize, Duration ttl, Clock clock) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be >= 1");
        }
        this.maxSize = maxSize;
        this.ttl = Objects.requireNonNull(ttl);
        this.clock = Objects.requireNonNull(clock);
        this.map = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > TtlLruCache.this.maxSize;
            }
        };
    }

    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            CacheEntry<V> entry = map.get(key);
            if (entry == null) {
                return Optional.empty();
            }
            if (entry.expiresAt.isBefore(clock.instant())) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    map.remove(key);
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
                return Optional.empty();
            }
            return Optional.of(entry.value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Instant expiresAt = clock.instant().plus(ttl);
        lock.writeLock().lock();
        try {
            map.put(key, new CacheEntry<>(value, expiresAt));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void invalidate(K key) {
        lock.writeLock().lock();
        try {
            map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    private record CacheEntry<V>(V value, Instant expiresAt) {
    }
}
