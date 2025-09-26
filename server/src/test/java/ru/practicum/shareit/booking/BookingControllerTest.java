package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;

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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
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
    @DisplayName("GET /bookings/{id} — returns booking for owner/booker")
    void get_byId_ok() throws Exception {
        var resp = new BookingResponse(
                42L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                "WAITING",
                new BookingResponse.Booker(777L),
                new BookingResponse.ItemShort(10L, "Drill")
        );
        Mockito.when(bookingService.get(777L, 42L)).thenReturn(resp);

        mvc.perform(get("/bookings/{id}", 42)
                        .header(USER_HEADER, 777))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.booker.id").value(777))
                .andExpect(jsonPath("$.item.id").value(10));
    }

    @Test
    @DisplayName("GET /bookings?state=ALL — returns list for booker with pagination")
    void list_user_all_ok() throws Exception {
        var r1 = new BookingResponse(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                "WAITING", new BookingResponse.Booker(100L),
                new BookingResponse.ItemShort(10L, "A"));
        var r2 = new BookingResponse(2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                "APPROVED", new BookingResponse.Booker(100L),
                new BookingResponse.ItemShort(11L, "B"));

        Mockito.when(bookingService.listUser(eq(100L), any(), eq(20), eq(5)))
                .thenReturn(List.of(r1, r2));

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "20")
                        .param("size", "5")
                        .header(USER_HEADER, 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /bookings/owner?state=FUTURE — returns list for owner with pagination")
    void list_owner_future_ok() throws Exception {
        var r = new BookingResponse(7L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                "WAITING", new BookingResponse.Booker(200L),
                new BookingResponse.ItemShort(15L, "Item"));

        Mockito.when(bookingService.listOwner(eq(1L), any(), eq(0), eq(10)))
                .thenReturn(List.of(r));

        mvc.perform(get("/bookings/owner")
                        .param("state", "FUTURE")
                        .param("from", "0")
                        .param("size", "10")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].item.id").value(15));
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