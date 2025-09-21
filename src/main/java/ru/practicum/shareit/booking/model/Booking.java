package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

/**
 * JPA entity representing an item booking.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Booking start timestamp (maps to start_ts). */
    @Column(name = "start_ts", nullable = false)
    private Instant start;

    /** Booking end timestamp (maps to end_ts). */
    @Column(name = "end_ts", nullable = false)
    private Instant end;

    /** The item being booked. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item;

    /** The user who made the booking. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booker_id", nullable = false)
    @ToString.Exclude
    private User booker;

    /** Current booking status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;
}