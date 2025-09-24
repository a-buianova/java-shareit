package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.web.UserIdArgumentResolver;
import ru.practicum.shareit.common.web.WebConfig;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import({WebConfig.class, UserIdArgumentResolver.class})
@ActiveProfiles("test")
@DisplayName("BookingControllerTest (WebMvc slice)")
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingService bookingService;

    @Test
    @DisplayName("POST /bookings — 201 Created, response body mapped")
    void create_201_mapsBody() throws Exception {
        var dto = new BookingCreateDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        var resp = new BookingResponse(
                123L,
                dto.start(),
                dto.end(),
                "WAITING",
                new BookingResponse.Booker(777L),
                new BookingResponse.ItemShort(10L, "Drill")
        );

        Mockito.when(bookingService.create(eq(777L), any())).thenReturn(resp);

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(dto))
                        .header(USER_HEADER, 777))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(777))
                .andExpect(jsonPath("$.item.id").value(10));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=... — maps 'approved' param and header")
    void approve_ok() throws Exception {
        var resp = new BookingResponse(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                "APPROVED",
                new BookingResponse.Booker(5L),
                new BookingResponse.ItemShort(9L, "X")
        );

        Mockito.when(bookingService.approve(1L, 55L, true)).thenReturn(resp);

        mvc.perform(patch("/bookings/{id}", 55)
                        .param("approved", "true")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    @DisplayName("Missing X-Sharer-User-Id → 400 from argument resolver")
    void missingHeader_400() throws Exception {
        mvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Sharer-User-Id")));
        Mockito.verifyNoInteractions(bookingService);
    }

    @Test
    @DisplayName("Invalid state param → 400 (service not invoked)")
    void bad_state_400() throws Exception {
        mvc.perform(get("/bookings")
                        .param("state", "UNKNOWN")
                        .header(USER_HEADER, 7))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(bookingService);
    }
}