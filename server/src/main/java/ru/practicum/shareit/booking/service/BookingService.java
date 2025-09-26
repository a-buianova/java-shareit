package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateParam;

import java.util.List;

/**
 * Use-case API for managing bookings.
 */
public interface BookingService {

    BookingResponse create(Long userId, BookingCreateDto dto);

    BookingResponse approve(Long ownerId, Long bookingId, boolean approved);

    BookingResponse get(Long userId, Long bookingId);

    List<BookingResponse> listUser(Long userId, BookingStateParam state, int from, int size);

    List<BookingResponse> listOwner(Long ownerId, BookingStateParam state, int from, int size);
}