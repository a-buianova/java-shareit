package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ActiveProfiles("test")
@DisplayName("Gateway/BookingController — pagination & state validation")
class BookingGatewayPaginationValidationTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @MockBean BookingClient bookingClient;

    @Test
    @DisplayName("GET /bookings: from < 0")
    void user_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("GET /bookings: size <= 0")
    void user_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("GET /bookings: unknown state")
    void user_unknownState_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "INVALID")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 1))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("GET /bookings/owner: from < 0")
    void owner_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 2))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("GET /bookings/owner: size <= 0")
    void owner_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "-5")
                        .header(HDR, 2))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }

    @Test
    @DisplayName("GET /bookings/owner: unknown state")
    void owner_unknownState_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "???")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 2))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingClient);
    }
}