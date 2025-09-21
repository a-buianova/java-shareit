package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.dto.BookingStateParam;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.web.CurrentUserId;

import java.util.List;

/** REST controller for booking scenarios. */
@Validated
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    /** POST /bookings — create a booking request (status WAITING). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // тест ждёт 201
    public BookingResponse create(@CurrentUserId Long userId,
                                  @RequestBody @Valid BookingCreateDto dto) {
        return service.create(userId, dto);
    }

    /** PATCH /bookings/{id}?approved={true|false} — owner approves/rejects. */
    @PatchMapping("/{bookingId}")
    public BookingResponse approve(@CurrentUserId Long ownerId,
                                   @PathVariable Long bookingId,
                                   @RequestParam("approved") boolean approved) {
        return service.approve(ownerId, bookingId, approved);
    }

    /** GET /bookings/{id} — visible to booker or owner. */
    @GetMapping("/{bookingId}")
    public BookingResponse get(@CurrentUserId Long userId,
                               @PathVariable Long bookingId) {
        return service.get(userId, bookingId);
    }

    /** GET /bookings?state=... — current user's bookings. */
    @GetMapping
    public List<BookingResponse> listUser(@CurrentUserId Long userId,
                                          @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                          @RequestParam(name = "from",  defaultValue = "0")  @PositiveOrZero int from,
                                          @RequestParam(name = "size",  defaultValue = "20") @Positive      int size) {
        BookingStateParam state = BookingStateParam.from(stateParam);
        return service.listUser(userId, state, from, size);
    }

    /** GET /bookings/owner?state=... — bookings for items of current owner. */
    @GetMapping("/owner")
    public List<BookingResponse> listOwner(@CurrentUserId Long ownerId,
                                           @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                           @RequestParam(name = "from",  defaultValue = "0")  @PositiveOrZero int from,
                                           @RequestParam(name = "size",  defaultValue = "20") @Positive      int size) {
        BookingStateParam state = BookingStateParam.from(stateParam);
        return service.listOwner(ownerId, state, from, size);
    }
}