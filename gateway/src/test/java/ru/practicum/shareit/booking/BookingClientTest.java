package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * Unit tests for {@link ru.practicum.shareit.booking.client.BookingClient}.
 * <p>
 * Uses {@link MockRestServiceServer} to verify that the client
 * builds correct HTTP requests: URL, method, headers, query params and body.
 */
@DisplayName("BookingClient: unit tests for HTTP contract")
class BookingClientTest {

    private static final String BASE = "http://localhost:9999"; // fake server root
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private TestableBookingClient client;
    private MockRestServiceServer server;

    /**
     * A small test subclass to expose the underlying RestTemplate.
     */
    private static class TestableBookingClient extends BookingClient {
        public TestableBookingClient(String serverUrl, RestTemplateBuilder builder) {
            super(serverUrl, builder);
        }
        RestTemplate rt() { return this.rest; }
    }

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10));

        client = new TestableBookingClient(BASE, builder);
        server = MockRestServiceServer.createServer(client.rt());
    }

    @Test
    @DisplayName("bookItem(): POST /bookings with JSON body and user header")
    void bookItem_postsBody_andHeader() throws Exception {
        long userId = 77L;
        BookItemRequestDto dto = new BookItemRequestDto(
                15L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0)
        );

        server.expect(once(), requestTo(new URI(BASE + "/bookings")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_HEADER, String.valueOf(userId)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemId").value(15))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andRespond(withStatus(CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("""
                              {"id":123,"status":"WAITING","booker":{"id":77},"item":{"id":15,"name":"Drill"}}
                              """));

        client.bookItem(userId, dto);
        server.verify();
    }

    @Test
    @DisplayName("getBooking(): GET /bookings/{id} with user header")
    void getBooking_sendsHeader() throws Exception {
        long userId = 5L;
        long bookingId = 999L;

        server.expect(once(), requestTo(new URI(BASE + "/bookings/" + bookingId)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, String.valueOf(userId)))
                .andRespond(withStatus(OK).contentType(MediaType.APPLICATION_JSON).body("{}"));

        client.getBooking(userId, bookingId);
        server.verify();
    }

    @Test
    @DisplayName("getBookings(): GET /bookings?state=...&from=...&size=... with user header")
    void getBookings_withQueryParams() throws Exception {
        long userId = 42L;

        server.expect(once(), requestTo(new URI(BASE + "/bookings?state=FUTURE&from=20&size=10")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, String.valueOf(userId)))
                .andRespond(withStatus(OK).contentType(MediaType.APPLICATION_JSON).body("[]"));

        client.getBookings(userId, BookingState.FUTURE, 20, 10);
        server.verify();
    }

    @Test
    @DisplayName("getOwnerBookings(): GET /bookings/owner?state=...&from=...&size=... with user header")
    void getOwnerBookings_withQueryParams() throws Exception {
        long ownerId = 1L;

        server.expect(once(), requestTo(new URI(BASE + "/bookings/owner?state=ALL&from=0&size=5")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, String.valueOf(ownerId)))
                .andRespond(withStatus(OK).contentType(MediaType.APPLICATION_JSON).body("[]"));

        client.getOwnerBookings(ownerId, BookingState.ALL, 0, 5);
        server.verify();
    }

    @Test
    @DisplayName("approve(): PATCH /bookings/{id}?approved=true with user header")
    void approve_patch_true() throws Exception {
        long ownerId = 10L;
        long bookingId = 321L;

        server.expect(once(), requestTo(new URI(BASE + "/bookings/" + bookingId + "?approved=true")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(USER_HEADER, String.valueOf(ownerId)))
                .andRespond(withStatus(OK).contentType(MediaType.APPLICATION_JSON).body("""
                        {"id":321,"status":"APPROVED"}
                        """));

        client.approve(ownerId, bookingId, true);
        server.verify();
    }
}