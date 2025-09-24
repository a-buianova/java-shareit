package ru.practicum.shareit.booking.dto;

/** Allowed values for the "state" query parameter (listing filter). */
public enum BookingStateParam {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingStateParam from(String value) {
        try {
            return value == null ? ALL : BookingStateParam.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown state: " + value);
        }
    }
}