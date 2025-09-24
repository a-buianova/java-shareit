package ru.practicum.shareit.client;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BaseClient {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final List<String> FORWARDED_HDRS =
            List.of(HttpHeaders.LOCATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.RETRY_AFTER);

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, @Nullable Long userId, @Nullable Map<String, Object> parameters) {
        return exchange(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, @Nullable Long userId,
                                              @Nullable Map<String, Object> parameters, T body) {
        return exchange(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId, T body) {
        return put(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId,
                                             @Nullable Map<String, Object> parameters, T body) {
        return exchange(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return patch(path, null, null, body);
    }

    protected ResponseEntity<Object> patch(String path, long userId) {
        return patch(path, userId, null, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, @Nullable Long userId,
                                               @Nullable Map<String, Object> parameters, T body) {
        return exchange(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return delete(path, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return delete(path, userId, null);
    }

    protected ResponseEntity<Object> delete(String path, @Nullable Long userId,
                                            @Nullable Map<String, Object> parameters) {
        return exchange(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private <T> ResponseEntity<Object> exchange(HttpMethod method,
                                                String path,
                                                @Nullable Long userId,
                                                @Nullable Map<String, Object> parameters,
                                                @Nullable T body) {
        HttpEntity<T> requestEntity = entityFor(userId, body);
        Map<String, Object> safeParams = sanitizeParams(parameters);

        try {
            ResponseEntity<Object> response = (safeParams != null)
                    ? rest.exchange(path, method, requestEntity, Object.class, safeParams)
                    : rest.exchange(path, method, requestEntity, Object.class);

            return proxyResponse(response);
        } catch (HttpStatusCodeException e) {
            ResponseEntity.BodyBuilder builder = ResponseEntity.status(e.getStatusCode());
            copyHeaders(e.getResponseHeaders(), builder);
            Object errorBody = e.getResponseBodyAsByteArray();
            return builder.body(errorBody == null ? new byte[0] : errorBody);
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Gateway cannot reach ShareIt server: " + e.getClass().getSimpleName()));
        }
    }

    private <T> HttpEntity<T> entityFor(@Nullable Long userId, @Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) headers.set(USER_HEADER, String.valueOf(userId));
        if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private static ResponseEntity<Object> proxyResponse(ResponseEntity<Object> response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(response.getStatusCode());
        copyHeaders(response.getHeaders(), builder);
        return response.hasBody() ? builder.body(response.getBody()) : builder.build();
    }

    private static void copyHeaders(@Nullable HttpHeaders from, ResponseEntity.BodyBuilder to) {
        if (from == null) return;
        for (String key : FORWARDED_HDRS) {
            List<String> values = from.get(key);
            if (values != null && !values.isEmpty()) {
                to.header(key, values.toArray(new String[0]));
            }
        }
    }

    private static @Nullable Map<String, Object> sanitizeParams(@Nullable Map<String, Object> params) {
        if (params == null || params.isEmpty()) return null;
        return params.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}