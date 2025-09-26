package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@ActiveProfiles("test")
@DisplayName("Gateway/BookingController — happy path (client forwarding)")
class BookingGatewayControllerHappyPathTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingClient bookingClient;

    @Test
    @DisplayName("POST /bookings — 201 Created; body is proxied")
    void create_201_proxiesBody() throws Exception {
        var dto = new BookItemRequestDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        var body = Map.of(
                "id", 123,
                "status", "WAITING",
                "item", Map.of("id", 10, "name", "Drill"),
                "booker", Map.of("id", 777)
        );
        when(bookingClient.bookItem(eq(777L), any())).thenReturn(ResponseEntity.status(201).body(body));

        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HDR, 777)
                        .content(om.writeValueAsBytes(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(10))
                .andExpect(jsonPath("$.booker.id").value(777));

        verify(bookingClient).bookItem(eq(777L), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=true — 200 OK")
    void approve_true_200() throws Exception {
        var body = Map.of("id", 55, "status", "APPROVED");
        when(bookingClient.approve(1L, 55L, true)).thenReturn(ResponseEntity.ok(body));

        mvc.perform(patch("/bookings/{id}", 55)
                        .param("approved", "true")
                        .header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).approve(1L, 55L, true);
    }

    @Test
    @DisplayName("GET /bookings/{id} — 200 OK")
    void getById_200() throws Exception {
        var body = Map.of("id", 99, "status", "WAITING");
        when(bookingClient.getBooking(7L, 99L)).thenReturn(ResponseEntity.ok(body));

        mvc.perform(get("/bookings/{id}", 99)
                        .header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(bookingClient).getBooking(7L, 99L);
    }

    @Test
    @DisplayName("GET /bookings?state=ALL — 200 OK")
    void listUser_all_200() throws Exception {
        var body = List.of(Map.of("id", 1), Map.of("id", 2));
        when(bookingClient.getBookings(7L, BookingState.ALL, 0, 10))
                .thenReturn(ResponseEntity.ok(body));

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .header(HDR, 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingClient).getBookings(7L, BookingState.ALL, 0, 10);
    }

    @Test
    @DisplayName("GET /bookings/owner?state=ALL — 200 OK")
    void listOwner_all_200() throws Exception {
        var body = List.of(Map.of("id", 3));
        when(bookingClient.getOwnerBookings(1L, BookingState.ALL, 5, 5))
                .thenReturn(ResponseEntity.ok(body));

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "5")
                        .param("size", "5")
                        .header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));

        verify(bookingClient).getOwnerBookings(1L, BookingState.ALL, 5, 5);
    }
}