package ru.practicum.shareit.booking.model;

/**
 * Booking lifecycle status as defined in sprint 14 requirements.
 */
public enum BookingStatus {
    /** New booking, waiting for owner approval. */
    WAITING,
    /** Booking confirmed by the owner. */
    APPROVED,
    /** Booking rejected by the owner. */
    REJECTED,
    /** Booking canceled by the booker. */
    CANCELED
}