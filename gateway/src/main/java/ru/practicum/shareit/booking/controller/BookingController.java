package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getBookings(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

		BookingState state = BookingState.from(stateParam); // throws on bad value -> 400 by handler
		log.info("Get bookings (booker) state={}, userId={}, from={}, size={}", state, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(
			@RequestHeader("X-Sharer-User-Id") long ownerId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

		BookingState state = BookingState.from(stateParam); // throws on bad value -> 400 by handler
		log.info("Get bookings (owner) state={}, ownerId={}, from={}, size={}", state, ownerId, from, size);
		return bookingClient.getOwnerBookings(ownerId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestBody @Valid BookItemRequestDto requestDto) {

		if (requestDto.getStart() != null && requestDto.getEnd() != null
				&& !requestDto.getStart().isBefore(requestDto.getEnd())) {
			throw new IllegalArgumentException("start must be before end");
		}
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(
			@RequestHeader("X-Sharer-User-Id") long ownerId,
			@PathVariable Long bookingId,
			@RequestParam("approved") boolean approved) {

		log.info("Approve booking id={}, ownerId={}, approved={}", bookingId, ownerId, approved);
		return bookingClient.approve(ownerId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {

		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}
}