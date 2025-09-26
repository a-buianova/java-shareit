package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

/**
 * API response DTO for a booking.
 * Dates are LocalDateTime in UTC. Mapper handles Instant <-> LocalDateTime.
 */
public record BookingResponse(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        String status,
        Booker booker,
        ItemShort item
) {
    /** Minimal booker view with id only. */
    public record Booker(Long id) {}

    /** Minimal item view with id and name. */
    public record ItemShort(Long id, String name) {}
}