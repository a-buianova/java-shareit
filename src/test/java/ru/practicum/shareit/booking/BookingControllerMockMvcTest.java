package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link BookingController}.
 * Controller has no implemented endpoints yet.
 */
@WebMvcTest(controllers = BookingController.class)
class BookingControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /bookings -> 405 Method Not Allowed (no endpoints)")
    void get_onBasePath_returns405() throws Exception {
        mvc.perform(get("/bookings"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /bookings -> 405 Method Not Allowed (no endpoints)")
    void post_onBasePath_returns405() throws Exception {
        mvc.perform(post("/bookings"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /bookings/{id} -> 404 Not Found (no mapping)")
    void get_onSubPath_returns404() throws Exception {
        mvc.perform(get("/bookings/123"))
                .andExpect(status().isNotFound());
    }
}