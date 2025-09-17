package ru.practicum.shareit.booking.mapper;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

/**
 * @apiNote Manual mapper for converting between {@link Booking} entities and DTOs.
 * @implNote Replaces MapStruct for simplicity in sprint 14.
 */
@Component
public final class BookingMapper {

    /**
     * Convert create DTO into a new {@link Booking} entity.
     * The {@code item} and {@code booker} are provided by service.
     */
    public Booking toEntity(BookingCreateDto dto, Item item, User booker) {
        return Booking.builder()
                .start(dto.start())
                .end(dto.end())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    /**
     * Convert {@link Booking} entity into response DTO.
     */
    public BookingResponse toResponse(@Nullable Booking b) {
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