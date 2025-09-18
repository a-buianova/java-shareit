package ru.practicum.shareit.booking.mapper;

import org.springframework.lang.Nullable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


/**
 * Lightweight manual mapper between {@link Booking} and its DTOs.
 * Implemented as a utility with static methods to avoid polluting Spring context.
 */

public final class BookingMapper {

    private BookingMapper() {
    }

    /**
     * Build a new {@link Booking} from create DTO and resolved associations.
     */

    public static Booking toEntity(BookingCreateDto dto, Item item, User booker) {
        return Booking.builder()
                .start(dto.start())
                .end(dto.end())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    /**
     * Convert {@link Booking} entity to response DTO (null-safe).
     */
    public static BookingResponse toResponse(@Nullable Booking b) {
        if (b == null) return null;
        return new BookingResponse(
                b.getId(),
                b.getStart(),
                b.getEnd(),
                b.getItem() != null ? b.getItem().getId() : null,
                b.getBooker() != null ? b.getBooker().getId() : null,
                b.getStatus() != null ? b.getStatus().name() : null
        );
    }
}