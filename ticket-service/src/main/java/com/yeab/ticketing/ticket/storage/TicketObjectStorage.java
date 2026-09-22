package com.yeab.ticketing.ticket.storage;

import java.io.IOException;
import java.util.UUID;

/**
 * Storage for generated ticket QR images. Backend is selected via
 * {@code app.storage.enabled}: MinIO when true, local filesystem otherwise.
 */
public interface TicketObjectStorage {

    /**
     * Persists the QR bytes for a ticket.
     *
     * @return the object's storage key
     */
    String store(UUID ticketId, byte[] data) throws IOException;

    byte[] get(UUID ticketId) throws IOException;

    default String keyFor(UUID ticketId) {
        return "tickets/" + ticketId + ".png";
    }
}