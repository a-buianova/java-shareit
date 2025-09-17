package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Domain model for an item booking.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class Booking {

    /** Surrogate primary key. */
    @EqualsAndHashCode.Include
    private Long id;

    /** Booking start timestamp (inclusive). */
    private LocalDateTime start;

    /** Booking end timestamp (exclusive). */
    private LocalDateTime end;

    /** The item being booked. */
    private Item item;

    /** The user who created the booking. */
    private User booker;

    /** Current lifecycle status of the booking. */
    private BookingStatus status;
}