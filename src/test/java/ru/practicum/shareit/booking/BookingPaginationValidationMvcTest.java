package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Booking: pagination/state validation")
class BookingPaginationValidationMvcTest {

    @Autowired
    private MockMvc mvc;

    private static final String HDR = "X-Sharer-User-Id";

    // -------- /bookings (booker) --------

    @Test
    @DisplayName("GET /bookings: from < 0 -> 400")
    void listUser_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings: size <= 0 -> 400")
    void listUser_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings: unknown state -> 400")
    void listUser_unknownState_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "SOMETHING")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
    }

    // -------- /bookings/owner (owner) --------

    @Test
    @DisplayName("GET /bookings/owner: from < 0 -> 400")
    void listOwner_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "-5")
                        .param("size", "10")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/owner: size <= 0 -> 400")
    void listOwner_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "-1")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/owner: unknown state -> 400")
    void listOwner_unknownState_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "INVALID")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
    }
}