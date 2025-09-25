package ru.practicum.shareit.item.dto;

import java.time.Instant;

/**
 * Minimal booking representation embedded into item details.
 * Uses {@link Instant} for time to be timezone-agnostic and unambiguous (UTC).
 */
import java.time.Instant;

/**
 * Minimal booking representation embedded into item details.
 * Uses {@link Instant} for time to be timezone-agnostic and unambiguous (UTC).
 */
public record BookingShortDto(
        Long id,
        Long bookerId,
        Instant start,
        Instant end
) {}