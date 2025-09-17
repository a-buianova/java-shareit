package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

/**
 * API response for a booking.
 *
 * @param id       booking ID
 * @param start    start time
 * @param end      end time
 * @param itemId   booked item ID
 * @param bookerId user ID of the booker
 * @param status   booking status (WAITING, APPROVED, REJECTED, CANCELED)
 */
public record BookingResponse(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        Long itemId,
        Long bookerId,
        String status
) {}