package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.common.exception.BadRequestException;

/** Allowed values for the "state" query parameter (listing filter). */
public enum BookingStateParam {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingStateParam from(String value) {
        if (value == null) return ALL;
        try {
            return BookingStateParam.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state: " + value);
        }
    }
}