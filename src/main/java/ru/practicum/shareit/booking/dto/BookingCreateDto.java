package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request payload for creating a booking.
 *
 * @param itemId ID of the item being booked (required)
 * @param start  booking start time (must be in the future)
 * @param end    booking end time (must be in the future)
 */
public record BookingCreateDto(
        @NotNull(message = "itemId is required")
        Long itemId,

        @NotNull(message = "start is required")
        @Future(message = "start must be in the future")
        LocalDateTime start,

        @NotNull(message = "end is required")
        @Future(message = "end must be in the future")
        LocalDateTime end
) {}