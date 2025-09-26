package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.time.Duration;
import java.util.Map;

/**
 * REST client for bookings API (gateway -> server).
 */
@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(trimTrailingSlash(serverUrl) + API_PREFIX))
                        .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                        .setConnectTimeout(Duration.ofSeconds(3))
                        .setReadTimeout(Duration.ofSeconds(10))
                        .build()
        );
    }

    public ResponseEntity<Object> getBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> params = Map.of("state", state.name(), "from", from, "size", size);
        return get("?state={state}&from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getOwnerBookings(long ownerId, BookingState state, Integer from, Integer size) {
        Map<String, Object> params = Map.of("state", state.name(), "from", from, "size", size);
        return get("/owner?state={state}&from={from}&size={size}", ownerId, params);
    }

    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approve(long ownerId, Long bookingId, boolean approved) {
        Map<String, Object> params = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", ownerId, params, null);
    }

    private static String trimTrailingSlash(String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }
}