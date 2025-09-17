package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BookingMapper}.
 */
@DisplayName("BookingMapper: unit tests")
class BookingMapperTest {

    private final BookingMapper mapper = new BookingMapper();

    @Test
    @DisplayName("toEntity(): creates WAITING booking with given item/booker")
    void toEntity_success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        Item item = new Item(5L, "Drill", "600W", true, null, null);
        User booker = new User(7L, "Bob", "b@ex.com");

        Booking booking = mapper.toEntity(dto, item, booker);

        assertNotNull(booking, "Booking should not be null");
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus(), "Default status must be WAITING");
    }

    @Test
    @DisplayName("toResponse(): flattens entity into response DTO")
    void toResponse_success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Item item = new Item(5L, "Drill", "600W", true, null, null);
        User booker = new User(7L, "Bob", "b@ex.com");

        Booking booking = Booking.builder()
                .id(42L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        BookingResponse resp = mapper.toResponse(booking);

        assertNotNull(resp, "Response must not be null");
        assertEquals(42L, resp.id());
        assertEquals(start, resp.start());
        assertEquals(end, resp.end());
        assertEquals(5L, resp.itemId());
        assertEquals(7L, resp.bookerId());
        assertEquals("WAITING", resp.status());
    }

    @Test
    @DisplayName("toResponse(): handles nulls gracefully")
    void toResponse_handlesNullsGracefully() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(null)
                .end(null)
                .item(null)
                .booker(null)
                .status(BookingStatus.REJECTED)
                .build();

        BookingResponse resp = mapper.toResponse(booking);

        assertNotNull(resp);
        assertEquals(1L, resp.id());
        assertNull(resp.start());
        assertNull(resp.end());
        assertNull(resp.itemId());
        assertNull(resp.bookerId());
        assertEquals("REJECTED", resp.status());
    }
}