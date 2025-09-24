package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.web.UserIdArgumentResolver;
import ru.practicum.shareit.common.web.WebConfig;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import({WebConfig.class, UserIdArgumentResolver.class})
@ActiveProfiles("test")
@DisplayName("BookingController: pagination/state validation (WebMvc slice)")
class BookingPaginationValidationMvcTest {

    @Autowired private MockMvc mvc;

    @MockBean private BookingService bookingService;

    private static final String HDR = "X-Sharer-User-Id";

    @Test
    @DisplayName("GET /bookings: from < 0 -> 400, сервис не вызывается")
    void listUser_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /bookings: size <= 0 -> 400, сервис не вызывается")
    void listUser_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /bookings: unknown state -> 400, сервис не вызывается")
    void listUser_unknownState_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "SOMETHING")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 100))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /bookings/owner: from < 0 -> 400, сервис не вызывается")
    void listOwner_fromNegative_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "-5")
                        .param("size", "10")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /bookings/owner: size <= 0 -> 400, сервис не вызывается")
    void listOwner_sizeNonPositive_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "-1")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("GET /bookings/owner: unknown state -> 400, сервис не вызывается")
    void listOwner_unknownState_400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .param("state", "INVALID")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 200))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }
}