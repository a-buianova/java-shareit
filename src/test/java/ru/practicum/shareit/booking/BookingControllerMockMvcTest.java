package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;

import java.util.stream.Stream;

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

    static Stream<String> unsupportedMethods() {
        return Stream.of("GET", "POST");
    }

    @ParameterizedTest(name = "{0} /bookings -> 405")
    @MethodSource("unsupportedMethods")
    @DisplayName("Base path /bookings rejects unsupported methods")
    void basePathUnsupportedMethods_return405(String method) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), "/bookings")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /bookings/{id} -> 404 (no mapping)")
    void get_onSubPath_returns404() throws Exception {
        mvc.perform(get("/bookings/123"))
                .andExpect(status().isNotFound());
    }
}