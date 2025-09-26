package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.time.Duration;
import java.util.Map;

/**
 * REST client for Items API (gateway -> server).
 * Uses HttpComponentsClientHttpRequestFactory to support PATCH.
 */
@Component
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit.server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(trimTrailingSlash(serverUrl) + API_PREFIX))
                        .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                        .setConnectTimeout(Duration.ofSeconds(3))
                        .setReadTimeout(Duration.ofSeconds(10))
                        .build()
        );
    }

    public ResponseEntity<Object> create(long ownerId, ItemCreateDto dto) {
        return post("", ownerId, dto);
    }

    public ResponseEntity<Object> update(long ownerId, long itemId, ItemUpdateDto dto) {
        return patch("/" + itemId, ownerId, dto);
    }

    public ResponseEntity<Object> getById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> listOwnerItems(long ownerId, Integer from, Integer size) {
        Map<String, Object> p = Map.of("from", from, "size", size);
        return get("?from={from}&size={size}", ownerId, p);
    }

    public ResponseEntity<Object> search(long userId, String text, Integer from, Integer size) {
        Map<String, Object> p = Map.of("text", text, "from", from, "size", size);
        return get("/search?text={text}&from={from}&size={size}", userId, p);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentCreateDto dto) {
        return post("/" + itemId + "/comment", userId, dto);
    }

    private static String trimTrailingSlash(String url) {
        return (url != null && url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
    }
}