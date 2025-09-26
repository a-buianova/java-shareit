package ru.practicum.shareit.request.dto;

import java.time.Instant;
import java.util.List;

/**
 * API response for an item request including items and requester info.
 * Primary JSON shape uses a nested "requester" object.
 */
public record ItemRequestResponse(
        Long id,
        String description,
        Instant created,
        Requester requester,
        List<ItemShortDto> items
) {
    /** Requester info required by Postman tests (id + name). */
    public record Requester(Long id, String name) {}

    /** Minimal item view in the context of a request. */
    public record ItemShortDto(
            Long id,
            String name,
            String description,
            boolean available,
            Long requestId
    ) {}

    /**
     * Backward-compatible convenience ctor: allows legacy calls that pass requestorId (Long)
     * instead of a Requester object. Name is set to null.
     */
    public ItemRequestResponse(Long id,
                               String description,
                               Long requestorId,
                               Instant created,
                               List<ItemShortDto> items) {
        this(id, description, created,
                requestorId == null ? null : new Requester(requestorId, null),
                items);
    }
}