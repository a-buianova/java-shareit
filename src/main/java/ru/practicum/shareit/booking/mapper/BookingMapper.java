package ru.practicum.shareit.booking.mapper;

import org.springframework.lang.Nullable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Maps between Booking entity (Instant) and API DTOs (LocalDateTime in system default zone).
 */
public final class BookingMapper {

    private BookingMapper() {}

    /** Build entity from create DTO; convert LocalDateTime -> Instant using system default zone. */
    public static Booking toEntity(BookingCreateDto dto, Item item, User booker) {
        var zone = ZoneId.systemDefault();
        return Booking.builder()
                .start(dto.start() == null ? null : dto.start().atZone(zone).toInstant())
                .end(dto.end() == null ? null : dto.end().atZone(zone).toInstant())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    /** Convert entity to response; convert Instant -> LocalDateTime using system default zone. */
    public static BookingResponse toResponse(@Nullable Booking b) {
        if (b == null) return null;

        var zone = ZoneId.systemDefault();

        LocalDateTime start = (b.getStart() == null) ? null
                : LocalDateTime.ofInstant(b.getStart(), zone);
        LocalDateTime end = (b.getEnd() == null) ? null
                : LocalDateTime.ofInstant(b.getEnd(), zone);

        BookingResponse.Booker booker = (b.getBooker() == null) ? null
                : new BookingResponse.Booker(b.getBooker().getId());

        BookingResponse.ItemShort item = (b.getItem() == null) ? null
                : new BookingResponse.ItemShort(b.getItem().getId(), b.getItem().getName());

        return new BookingResponse(
                b.getId(),
                start,
                end,
                b.getStatus() == null ? null : b.getStatus().name(),
                booker,
                item
        );
    }
}