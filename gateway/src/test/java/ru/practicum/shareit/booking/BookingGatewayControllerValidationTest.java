package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Negative validation tests for {@link BookingController} in Gateway.
 */
@WebMvcTest(controllers = BookingController.class)
@ActiveProfiles("test")
@DisplayName("Gateway/BookingController - validation (negative cases)")
class BookingGatewayControllerValidationTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingClient bookingClient;

    @Nested
    @DisplayName("POST /bookings")
    class Create {

        @Test
        @DisplayName("400 - missing X-Sharer-User-Id header")
        void missingHeader_400() throws Exception {
            var dto = new BookItemRequestDto(
                    1L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(2)
            );

            mvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsBytes(dto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookingClient);
        }

        @Test
        @DisplayName("400 - itemId = 0 (must be positive)")
        void itemId_zero_400() throws Exception {
            var dto = new BookItemRequestDto(
                    0L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(2)
            );

            mvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HDR, 10)
                            .content(om.writeValueAsBytes(dto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookingClient);
        }

        @Test
        @DisplayName("400 - start in the past")
        void start_inPast_400() throws Exception {
            var now = LocalDateTime.now();
            var dto = new BookItemRequestDto(1L, now.minusMinutes(1), now.plusHours(1));

            mvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HDR, 10)
                            .content(om.writeValueAsBytes(dto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookingClient);
        }

        @Test
        @DisplayName("400 - end is not after start")
        void end_notAfterStart_400() throws Exception {
            var t = LocalDateTime.now().plusDays(1);
            var dto = new BookItemRequestDto(1L, t, t);

            mvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HDR, 10)
                            .content(om.writeValueAsBytes(dto)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookingClient);
        }
    }

    @Nested
    @DisplayName("PATCH /bookings/{id}")
    class Approve {

        @Test
        @DisplayName("400 - missing required 'approved' parameter")
        void missingApprovedParam_400() throws Exception {
            mvc.perform(patch("/bookings/{id}", 55L)
                            .header(HDR, 1))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(bookingClient);
        }
    }
}