package com.yeab.ticketing.ticket.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Development fallback: stores QR images on the local filesystem so the service
 * and its tests run without MinIO.
 */
@Component
@ConditionalOnProperty(name = "app.storage.enabled", havingValue = "false", matchIfMissing = true)
public class LocalTicketObjectStorage implements TicketObjectStorage {

    private final Path baseDir;

    public LocalTicketObjectStorage(@Value("${app.storage.local-dir:./data/tickets}") String localDir) {
        this.baseDir = Path.of(localDir);
    }

    @Override
    public String store(UUID ticketId, byte[] data) throws IOException {
        Path target = resolve(ticketId);
        Files.createDirectories(target.getParent());
        Files.write(target, data);
        return keyFor(ticketId);
    }

    @Override
    public byte[] get(UUID ticketId) throws IOException {
        Path target = resolve(ticketId);
        if (!Files.exists(target)) {
            throw new IOException("QR image not found: " + keyFor(ticketId));
        }
        return Files.readAllBytes(target);
    }

    private Path resolve(UUID ticketId) {
        return baseDir.resolve(ticketId + ".png");
    }
}