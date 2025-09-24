package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

/**
 * REST client for users API (gateway -> server).
 */
@Component
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(UserCreateDto dto) {
        return post("", dto);
    }

    public ResponseEntity<Object> get(long id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> list() {
        return get("");
    }

    public ResponseEntity<Object> patch(long id, UserUpdateDto dto) {
        return patch("/" + id, dto);
    }

    public ResponseEntity<Object> delete(long id) {
        return delete("/" + id);
    }
}