package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Unit tests for ItemClient (verifies HTTP method, URL, query params, header forwarding).
 */
@DisplayName("ItemClient: unit tests with MockRestServiceServer")
class ItemClientTest {

    private ItemClient client;
    private MockRestServiceServer server;
    private RestTemplate rest;

    @BeforeEach
    void setUp() {
        // Build a RestTemplateBuilder similar to production
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8081/items"))
                .requestFactory(settings -> new HttpComponentsClientHttpRequestFactory())
                .setConnectTimeout(Duration.ofSeconds(1))
                .setReadTimeout(Duration.ofSeconds(2));

        client = new ItemClient("http://localhost:8081", builder);
        rest = (RestTemplate) ReflectionTestUtils.getField(client, "rest");
        server = MockRestServiceServer.createServer(rest);
    }

    @Test
    @DisplayName("create(): POST /items with X-Sharer-User-Id and body")
    void create_ok() {
        var dto = new ItemCreateDto("Drill", "600W", true, null);

        server.expect(once(), requestTo(URI.create("http://localhost:8081/items")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "5"))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":11,\"name\":\"Drill\"}"));

        var resp = client.create(5L, dto);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        server.verify();
    }

    @Test
    @DisplayName("update(): PATCH /items/{id}")
    void update_ok() {
        var dto = new ItemUpdateDto("New name", null, null);

        server.expect(once(), requestTo(URI.create("http://localhost:8081/items/11")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":11,\"name\":\"New name\"}", MediaType.APPLICATION_JSON));

        var resp = client.update(1L, 11L, dto);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    @DisplayName("getById(): GET /items/{id}")
    void getById_ok() {
        server.expect(once(), requestTo(URI.create("http://localhost:8081/items/11")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("{\"id\":11}", MediaType.APPLICATION_JSON));

        var resp = client.getById(2L, 11L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    @DisplayName("listOwnerItems(): GET /items?from&size")
    void listOwnerItems_ok() {
        server.expect(once(),
                        requestTo("http://localhost:8081/items?from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var resp = client.listOwnerItems(1L, 0, 10);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    @DisplayName("search(): GET /items/search?text=drill&from&size")
    void search_ok() {
        server.expect(once(),
                        requestTo("http://localhost:8081/items/search?text=drill&from=0&size=5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("[{\"id\":11}]", MediaType.APPLICATION_JSON));

        var resp = client.search(2L, "drill", 0, 5);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    @DisplayName("addComment(): POST /items/{id}/comment")
    void addComment_ok() {
        var dto = new CommentCreateDto("Nice!");

        server.expect(once(), requestTo(URI.create("http://localhost:8081/items/11/comment")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andExpect(jsonPath("$.text").value("Nice!"))
                .andRespond(withSuccess("{\"id\":100,\"text\":\"Nice!\"}", MediaType.APPLICATION_JSON));

        var resp = client.addComment(3L, 11L, dto);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        server.verify();
    }

    @Test
    @DisplayName("server returns 404 -> client propagates 404")
    void notFound_isPropagated() {
        server.expect(once(), requestTo(URI.create("http://localhost:8081/items/999")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).body("not found").contentType(MediaType.TEXT_PLAIN));

        var resp = client.getById(1L, 999L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        server.verify();
    }
}