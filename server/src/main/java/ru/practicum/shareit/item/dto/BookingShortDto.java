package ru.practicum.shareit.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Minimal booking representation embedded into item details.
 * Uses {@link Instant} for time to be timezone-agnostic and unambiguous (UTC).
 */
public record BookingShortDto(

        @Schema(description = "Booking ID", example = "123")
        Long id,

        @Schema(description = "Booker user ID", example = "42")
        Long bookerId,

        @Schema(description = "Start time (ISO-8601 instant, UTC)", example = "2030-01-01T10:00:00Z")
        Instant start,

        @Schema(description = "End time (ISO-8601 instant, UTC)", example = "2030-01-01T12:00:00Z")
        Instant end
) {}