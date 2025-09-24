package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.time.Duration;
import java.util.Map;

/**
 * REST client for item requests API (gateway -> server).
 */
@Service
public class RequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    public RequestClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                        .setConnectTimeout(Duration.ofSeconds(3))
                        .setReadTimeout(Duration.ofSeconds(10))
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ItemRequestCreateDto body) {
        return post("", userId, body);
    }

    public ResponseEntity<Object> findOwn(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findAllExceptUser(long userId, Integer from, Integer size) {
        Map<String, Object> params = Map.of("from", from, "size", size);
        return get("/all?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return get("/" + requestId, userId);
    }
}