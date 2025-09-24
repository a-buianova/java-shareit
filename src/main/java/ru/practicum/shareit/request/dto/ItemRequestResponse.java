package ru.practicum.shareit.request.dto;

import java.time.Instant;
import java.util.List;

/**
 * API response for an item request including items.
 * created — Instant (UTC). items — минимальное представление вещей.
 */
public record ItemRequestResponse(
        Long id,
        String description,
        Long requestorId,
        Instant created,
        List<ItemShortDto> items
) {
    /** Minimal item view in the context of a request. */
    public record ItemShortDto(
            Long id,
            String name,
            String description,
            boolean available,
            Long requestId
    ) {}
}