package ru.practicum.shareit.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @apiNote Placeholder for booking endpoints (sprint 14).
 * @implNote Declares base mapping so that GET/POST on "/bookings" return 405
 *           instead of falling into static resource handler (500).
 *           Real endpoints will be added in later sprints.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    /**
     * Dummy endpoint used only to let Spring register the base mapping.
     * Returning 501 makes it clear that the feature is not yet implemented.
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public void placeholder() {
        // no-op
    }
}