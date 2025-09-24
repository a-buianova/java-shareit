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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BookingMapper}.
 */
@DisplayName("BookingMapper: unit tests (LocalDateTime DTO ↔ Instant entity, system zone)")
class BookingMapperTest {

    @Test
    @DisplayName("toEntity(): creates WAITING booking with given item/booker")
    void toEntity_success() {
        LocalDateTime startLdt = LocalDateTime.now().plusDays(1);
        LocalDateTime endLdt   = LocalDateTime.now().plusDays(2);
        BookingCreateDto dto = new BookingCreateDto(1L, startLdt, endLdt);

        Item item = Item.builder()
                .id(5L)
                .name("Drill")
                .description("600W")
                .available(true)
                .build();

        User booker = User.builder()
                .id(7L)
                .name("Bob")
                .email("b@ex.com")
                .build();

        Booking booking = BookingMapper.toEntity(dto, item, booker);

        assertNotNull(booking, "Booking should not be null");

        var zone = ZoneId.systemDefault();
        Instant expectedStart = startLdt.atZone(zone).toInstant();
        Instant expectedEnd   = endLdt.atZone(zone).toInstant();

        assertEquals(expectedStart, booking.getStart());
        assertEquals(expectedEnd, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus(), "Default status must be WAITING");
    }

    @Test
    @DisplayName("toResponse(): maps entity to DTO with nested booker/item and system-zone LocalDateTime")
    void toResponse_success() {
        Instant start = Instant.now().plusSeconds(24 * 3600);
        Instant end   = Instant.now().plusSeconds(48 * 3600);

        Item item = Item.builder()
                .id(5L)
                .name("Drill")
                .description("600W")
                .available(true)
                .build();

        User booker = User.builder()
                .id(7L)
                .name("Bob")
                .email("b@ex.com")
                .build();

        Booking booking = Booking.builder()
                .id(42L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        BookingResponse resp = BookingMapper.toResponse(booking);

        assertNotNull(resp, "Response must not be null");
        assertEquals(42L, resp.id());

        var zone = ZoneId.systemDefault();
        LocalDateTime expectedStart = LocalDateTime.ofInstant(start, zone);
        LocalDateTime expectedEnd   = LocalDateTime.ofInstant(end, zone);
        assertEquals(expectedStart, resp.start());
        assertEquals(expectedEnd, resp.end());

        assertNotNull(resp.booker());
        assertEquals(7L, resp.booker().id());

        assertNotNull(resp.item());
        assertEquals(5L, resp.item().id());
        assertEquals("Drill", resp.item().name());

        assertEquals("WAITING", resp.status());
    }

    @Test
    @DisplayName("toResponse(): handles nulls gracefully with nested fields")
    void toResponse_handlesNullsGracefully() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(null)
                .end(null)
                .item(null)
                .booker(null)
                .status(BookingStatus.REJECTED)
                .build();

        BookingResponse resp = BookingMapper.toResponse(booking);

        assertNotNull(resp);
        assertEquals(1L, resp.id());
        assertNull(resp.start());
        assertNull(resp.end());
        assertNull(resp.item());
        assertNull(resp.booker());
        assertEquals("REJECTED", resp.status());
    }
}