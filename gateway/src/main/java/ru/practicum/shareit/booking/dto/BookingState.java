package ru.practicum.shareit.booking.dto;

public enum BookingState {
	ALL,        // all bookings
	CURRENT,    // bookings active now
	FUTURE,     // bookings in the future
	PAST,       // finished bookings
	REJECTED,   // rejected by owner
	WAITING;    // waiting for approval

	/**
	 * Parses state string ignoring case.
	 * Throws IllegalArgumentException with required message if state is unknown.
	 */
	public static BookingState from(String stringState) {
		if (stringState == null) {
			throw new IllegalArgumentException("Unknown state: null");
		}
		for (BookingState state : values()) {
			if (state.name().equalsIgnoreCase(stringState)) {
				return state;
			}
		}
		throw new IllegalArgumentException("Unknown state: " + stringState);
	}
}