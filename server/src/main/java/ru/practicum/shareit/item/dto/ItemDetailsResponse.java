package ru.practicum.shareit.item.dto;

import java.util.List;

/**
 * Detailed item representation used in:
 * - GET /items/{itemId} (comments always; bookings if requester is the owner),
 * - GET /items (owner's list: includes last/next with comments).
 */
public record ItemDetailsResponse(
        Long id,
        String name,
        String description,
        boolean available,
        BookingShortDto lastBooking,
        BookingShortDto nextBooking,
        List<CommentResponse> comments
) { }